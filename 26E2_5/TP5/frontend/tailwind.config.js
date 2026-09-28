/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  corePlugins: {
    preflight: false, // Do not override existing custom styles in App.css and index.css
  },
  theme: {
    extend: {},
  },
  plugins: [],
}
