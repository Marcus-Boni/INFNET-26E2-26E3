import os
from PIL import Image, ImageDraw, ImageFont
import pygments
from pygments.lexers import JavaLexer
from pygments.token import Token

FONT_MONO_PATH = "C:/Windows/Fonts/consola.ttf"
FONT_UI_PATH = "C:/Windows/Fonts/segoeui.ttf"
FONT_UI_BOLD_PATH = "C:/Windows/Fonts/segoeuib.ttf"

# VS Code Dark+ Color Palette
THEME = {
    "bg": (30, 30, 30),
    "title_bg": (45, 45, 45),
    "tab_active_bg": (30, 30, 30),
    "tab_inactive_bg": (45, 45, 45),
    "tab_border_top": (0, 122, 204),
    "gutter_bg": (30, 30, 30),
    "gutter_fg": (133, 133, 133),
    "gutter_border": (45, 45, 45),
    "status_bg": (0, 122, 204),
    "status_fg": (255, 255, 255),
    "default_fg": (212, 212, 212),
    "text_muted": (150, 150, 150),
    
    # Token colors
    Token.Keyword: (197, 134, 192),          # #C586C0 (purple)
    Token.Keyword.Constant: (86, 156, 214),  # #569CD6 (blue)
    Token.Keyword.Declaration: (86, 156, 214),
    Token.Keyword.Namespace: (197, 134, 192),
    Token.Keyword.Type: (78, 201, 176),      # #4EC9B0 (cyan/green)
    Token.Name.Class: (78, 201, 176),
    Token.Name.Function: (220, 220, 170),    # #DCDCAA (yellow)
    Token.Name.Decorator: (220, 220, 170),
    Token.Name.Variable: (156, 220, 254),    # #9CDCFE (light blue)
    Token.Name: (156, 220, 254),
    Token.String: (206, 145, 120),            # #CE9178 (orange/brown)
    Token.Number: (181, 206, 168),            # #B5CEA8 (light green)
    Token.Comment: (106, 153, 85),           # #6A9955 (green)
    Token.Comment.Single: (106, 153, 85),
    Token.Comment.Multiline: (106, 153, 85),
    Token.Punctuation: (212, 212, 212),
    Token.Operator: (212, 212, 212),
}

def get_token_color(token_type):
    while token_type:
        if token_type in THEME:
            return THEME[token_type]
        token_type = token_type.parent
    return THEME["default_fg"]

def render_code_screenshot(code_text, filename, breadcrumb, output_image_path, width=980):
    lines = code_text.replace("\r\n", "\n").strip().split("\n")
    
    font_mono = ImageFont.truetype(FONT_MONO_PATH, 14)
    font_ui = ImageFont.truetype(FONT_UI_PATH, 12)
    font_ui_bold = ImageFont.truetype(FONT_UI_BOLD_PATH, 12)
    
    # Dimensions
    title_height = 36
    tab_height = 32
    breadcrumb_height = 24
    header_total_height = title_height + tab_height + breadcrumb_height
    status_height = 24
    line_height = 20
    gutter_width = 54
    padding_x = 16
    padding_y = 10
    
    content_height = len(lines) * line_height + padding_y * 2
    total_height = header_total_height + content_height + status_height
    
    img = Image.new("RGB", (width, total_height), THEME["bg"])
    draw = ImageDraw.Draw(img)
    
    # 1. Title bar (Window top)
    draw.rectangle([0, 0, width, title_height], fill=THEME["title_bg"])
    # macOS style traffic lights
    draw.ellipse([14, 12, 26, 24], fill=(255, 95, 86))    # Close
    draw.ellipse([34, 12, 46, 24], fill=(255, 189, 46))   # Minimize
    draw.ellipse([54, 12, 66, 24], fill=(39, 201, 63))    # Maximize
    # Title text
    window_title = f"{filename} - Infnet Microservices Architecture (TP3)"
    title_bbox = draw.textbbox((0, 0), window_title, font=font_ui)
    title_w = title_bbox[2] - title_bbox[0]
    draw.text(((width - title_w) // 2, 10), window_title, font=font_ui, fill=THEME["text_muted"])
    
    # 2. Tab bar
    tab_y = title_height
    draw.rectangle([0, tab_y, width, tab_y + tab_height], fill=THEME["tab_inactive_bg"])
    # Active tab
    active_tab_width = 240
    draw.rectangle([0, tab_y, active_tab_width, tab_y + tab_height], fill=THEME["tab_active_bg"])
    draw.rectangle([0, tab_y, active_tab_width, tab_y + 2], fill=THEME["tab_border_top"])
    # File icon indicator (Java coffee icon placeholder / symbol)
    draw.text((14, tab_y + 8), "☕", font=font_ui, fill=(234, 102, 102))
    draw.text((36, tab_y + 8), filename, font=font_ui_bold, fill=THEME["default_fg"])
    draw.text((active_tab_width - 22, tab_y + 8), "×", font=font_ui, fill=THEME["text_muted"])
    
    # 3. Breadcrumbs
    bc_y = tab_y + tab_height
    draw.rectangle([0, bc_y, width, bc_y + breadcrumb_height], fill=THEME["bg"])
    draw.line([0, bc_y + breadcrumb_height - 1, width, bc_y + breadcrumb_height - 1], fill=(40, 40, 40))
    draw.text((14, bc_y + 4), breadcrumb, font=font_ui, fill=THEME["text_muted"])
    
    # 4. Code Area and Gutter
    code_start_y = header_total_height + padding_y
    draw.rectangle([0, header_total_height, gutter_width, header_total_height + content_height], fill=THEME["gutter_bg"])
    draw.line([gutter_width, header_total_height, gutter_width, header_total_height + content_height], fill=THEME["gutter_border"])
    
    # Lex the code
    lexer = JavaLexer()
    tokens = list(pygments.lex(code_text, lexer=lexer))
    
    # Draw line numbers
    for idx in range(len(lines)):
        y = code_start_y + idx * line_height
        line_num = str(idx + 1)
        num_bbox = draw.textbbox((0, 0), line_num, font=font_mono)
        num_w = num_bbox[2] - num_bbox[0]
        draw.text((gutter_width - num_w - 12, y), line_num, font=font_mono, fill=THEME["gutter_fg"])
        
    # Draw tokenized code
    curr_line = 0
    curr_x = gutter_width + padding_x
    curr_y = code_start_y
    
    for token_type, token_value in tokens:
        color = get_token_color(token_type)
        parts = token_value.split("\n")
        for p_idx, part in enumerate(parts):
            if p_idx > 0:
                curr_line += 1
                curr_x = gutter_width + padding_x
                curr_y = code_start_y + curr_line * line_height
            if part:
                draw.text((curr_x, curr_y), part, font=font_mono, fill=color)
                part_bbox = draw.textbbox((0, 0), part, font=font_mono)
                part_w = part_bbox[2] - part_bbox[0]
                curr_x += part_w
                
    # 5. Status Bar
    status_y = total_height - status_height
    draw.rectangle([0, status_y, width, total_height], fill=THEME["status_bg"])
    # Status bar text
    status_left = f" ⎇ main  |  Ln 1, Col 1  |  UTF-8  |  Java 17  |  Spring Boot  |  Infnet"
    draw.text((10, status_y + 4), status_left, font=font_ui, fill=THEME["status_fg"])
    status_right = f"{len(lines)} lines "
    right_bbox = draw.textbbox((0, 0), status_right, font=font_ui)
    right_w = right_bbox[2] - right_bbox[0]
    draw.text((width - right_w - 10, status_y + 4), status_right, font=font_ui, fill=THEME["status_fg"])
    
    os.makedirs(os.path.dirname(output_image_path), exist_ok=True)
    img.save(output_image_path, "PNG", quality=95)
    print(f"Gerado com sucesso: {output_image_path}")

def main():
    base_dir = r"c:\Users\mgalv\Projetos-Programacao\Projetos-Faculdade\Engenharia-Disciplinada-Periodo7\INFNET-26E2-26E3\26E3_4\TP3"
    img_dir = os.path.join(base_dir, "images")
    
    # 1. Idempotent Consumer
    idempotent_file = os.path.join(base_dir, "src", "main", "java", "br", "edu", "infnet", "messaging", "idempotency", "IdempotentConsumerService.java")
    with open(idempotent_file, "r", encoding="utf-8") as f:
        code_idempotent = f.read()
    render_code_screenshot(
        code_idempotent,
        "IdempotentConsumerService.java",
        "TP3 > src > main > java > br > edu > infnet > messaging > idempotency > IdempotentConsumerService.java > processMessage()",
        os.path.join(img_dir, "ide_idempotent_consumer.png")
    )
    
    # 2. Transactional Outbox
    outbox_file = os.path.join(base_dir, "src", "main", "java", "br", "edu", "infnet", "messaging", "outbox", "OrderOutboxService.java")
    with open(outbox_file, "r", encoding="utf-8") as f:
        code_outbox = f.read()
    render_code_screenshot(
        code_outbox,
        "OrderOutboxService.java",
        "TP3 > src > main > java > br > edu > infnet > messaging > outbox > OrderOutboxService.java > createOrder()",
        os.path.join(img_dir, "ide_transactional_outbox.png")
    )
    
    # 3. Saga Orchestrator
    saga_file = os.path.join(base_dir, "src", "main", "java", "br", "edu", "infnet", "messaging", "saga", "OrderSagaOrchestrator.java")
    with open(saga_file, "r", encoding="utf-8") as f:
        code_saga = f.read()
    render_code_screenshot(
        code_saga,
        "OrderSagaOrchestrator.java",
        "TP3 > src > main > java > br > edu > infnet > messaging > saga > OrderSagaOrchestrator.java > executeSaga()",
        os.path.join(img_dir, "ide_saga_orchestrator.png")
    )

if __name__ == "__main__":
    main()
