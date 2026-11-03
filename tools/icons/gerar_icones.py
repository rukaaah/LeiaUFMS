"""Gera o conjunto de ícones LeiaUFMS (grade 24, traço 2, pontas e junções redondas, raio 2).

Uso: python3 tools/icons/gerar_icones.py app/src/main/res/drawable docs/icones/folha.html docs/icones/svg
Regras e inventário em docs/09-icones.md.
"""
import os, sys, re
OUT = sys.argv[1]
SHEET = sys.argv[2]
S, F = 'stroke', 'fill'

def circle(cx, cy, r):
    return f"M{cx-r},{cy}a{r},{r} 0 1,0 {2*r},0a{r},{r} 0 1,0 {-2*r},0"

def dot(cx, cy, r=1.25):
    return (circle(cx, cy, r), F)

HEART = "M12,20.5C12,20.5 3.5,15.6 3.5,9.6A4.6,4.6 0 0,1 12,7.1A4.6,4.6 0 0,1 20.5,9.6C20.5,15.6 12,20.5 12,20.5Z"
STAR = "M12,3.5L14.6,8.8L20.4,9.6L16.2,13.7L17.2,19.5L12,16.8L6.8,19.5L7.8,13.7L3.6,9.6L9.4,8.8Z"
HOUSE = "M4,10.5L12,4L20,10.5V19A2,2 0 0,1 18,21H15V15H9V21H6A2,2 0 0,1 4,19Z"
BELL = "M6,16.5V11A6,6 0 0,1 18,11V16.5L19.5,18H4.5Z"
PIN = "M12,21.5C12,21.5 5,15.4 5,9.8A7,7 0 0,1 19,9.8C19,15.4 12,21.5 12,21.5Z"
BUBBLE_BACK = "M5,3H13A2,2 0 0,1 15,5V11A2,2 0 0,1 13,13H9L5,16V13A2,2 0 0,1 3,11V5A2,2 0 0,1 5,3Z"
BUBBLE_FRONT = "M15,8.5H19A2,2 0 0,1 21,10.5V16.5A2,2 0 0,1 19,18.5V21L15.5,18.5H11A2,2 0 0,1 9,16.5V15.5"
PERSON_HEAD = circle(12, 8, 4)
PERSON_BODY = "M4,21A8,8 0 0,1 20,21"
THUMB = ["M7,10V21H4.5A1.5,1.5 0 0,1 3,19.5V11.5A1.5,1.5 0 0,1 4.5,10Z",
         "M7,10L10.5,3.5A2.5,2.5 0 0,1 13.5,6V9H18.6A2,2 0 0,1 20.6,11.3L19.2,19.3A2,2 0 0,1 17.2,21H7"]

ICONS = {
  # Navegação
  'ic_home': [(HOUSE, S)],
  'ic_home_filled': [("M4,10.5L12,4L20,10.5V19A2,2 0 0,1 18,21H15V15H9V21H6A2,2 0 0,1 4,19Z", 'fillstroke')],
  'ic_swap': [("M4,8H19", S), ("M15,4L19,8L15,12", S), ("M20,16H5", S), ("M9,12L5,16L9,20", S)],
  'ic_forum': [(BUBBLE_BACK, S), (BUBBLE_FRONT, S)],
  'ic_forum_filled': [(BUBBLE_BACK, 'fillstroke'), ("M15,8.5H19A2,2 0 0,1 21,10.5V16.5A2,2 0 0,1 19,18.5V21L15.5,18.5H11A2,2 0 0,1 9,16.5V15.5H13A2,2 0 0,0 15,13.5Z", 'fillstroke')],
  'ic_person': [(PERSON_HEAD, S), (PERSON_BODY, S)],
  'ic_person_filled': [(PERSON_HEAD, 'fillstroke'), (PERSON_BODY + "Z", 'fillstroke')],
  'ic_favorite_border': [(HEART, S)],
  'ic_favorite': [(HEART, 'fillstroke')],
  'ic_back': [("M19,12H5", S), ("M11,6L5,12L11,18", S)],
  'ic_chevron_right': [("M9,6L15,12L9,18", S)],
  'ic_close': [("M6,6L18,18", S), ("M18,6L6,18", S)],
  'ic_add': [("M12,5V19", S), ("M5,12H19", S)],
  'ic_search': [(circle(10.5, 10.5, 6.5), S), ("M15.5,15.5L20.5,20.5", S)],
  'ic_explore': [(circle(12, 12, 9), S), ("M15.5,8.5L13.5,13.5L8.5,15.5L10.5,10.5Z", S)],
  'ic_settings': [("M4,6H13", S), ("M17,6H20", S), ("M4,12H7", S), ("M11,12H20", S), ("M4,18H15", S), ("M19,18H20", S),
                  (circle(15, 6, 2), S), (circle(9, 12, 2), S), (circle(17, 18, 2), S)],
  'ic_tune': [("M3.5,5H20.5L14,12.5V19L10,21V12.5Z", S)],
  'ic_logout': [("M9,21H5A2,2 0 0,1 3,19V5A2,2 0 0,1 5,3H9", S), ("M16,17L21,12L16,7", S), ("M21,12H9", S)],
  # Livros e leitura
  'ic_book': [("M12,7V20.5", S), ("M12,7A4,4 0 0,0 8,4H3V18H9A3,3 0 0,1 12,20.5", S), ("M12,7A4,4 0 0,1 16,4H21V18H15A3,3 0 0,0 12,20.5", S)],
  'ic_library': [("M5,4H8A1,1 0 0,1 9,5V20H4V5A1,1 0 0,1 5,4Z", S), ("M9,7H13A1,1 0 0,1 14,8V20H9", S), ("M14.5,6.5L17.6,5.7L21,19.2L17.9,20L14.5,6.5Z", S), ("M3,20H21", S)],
  'ic_local_library': [(circle(12, 5.5, 2.5), S), ("M12,12.5V21", S), ("M3,10A15,15 0 0,1 12,12.5A15,15 0 0,1 21,10V18.5A15,15 0 0,0 12,21A15,15 0 0,0 3,18.5Z", S)],
  'ic_bookmark_border': [("M7,3H17A1,1 0 0,1 18,4V21L12,17L6,21V4A1,1 0 0,1 7,3Z", S)],
  'ic_bookmark': [("M7,3H17A1,1 0 0,1 18,4V21L12,17L6,21V4A1,1 0 0,1 7,3Z", 'fillstroke')],
  'ic_school': [("M2,9L12,4L22,9L12,14Z", S), ("M6,11V16C6,17.7 8.7,19 12,19C15.3,19 18,17.7 18,16V11", S), ("M22,9V14.5", S)],
  'ic_donate': [("M12,12.5C12,12.5 8,10.1 8,7.4A2,2 0 0,1 12,6.1A2,2 0 0,1 16,7.4C16,10.1 12,12.5 12,12.5Z", S),
                ("M2,14.5H5V21H2", S), ("M5,15.5H8L11,16.5H14A1.5,1.5 0 0,1 14,19.5H10", S), ("M14,18.5L18.4,16.4A1.6,1.6 0 0,1 20.2,18.9L15.5,21H5", S)],
  # Encontro e lugar
  'ic_place': [(PIN, S), (circle(12, 9.8, 2.5), S)],
  'ic_location_off': [("M8.2,4.2A7,7 0 0,1 19,9.8C19,11.6 18.2,13.4 17.2,15", S), ("M14.8,18.8C13.2,20.5 12,21.5 12,21.5C12,21.5 5,15.4 5,9.8C5,9 5.1,8.3 5.4,7.6", S), ("M3,3L21,21", S)],
  'ic_my_location': [(circle(12, 12, 7), S), (circle(12, 12, 2.5), S), ("M12,2V5", S), ("M12,19V22", S), ("M2,12H5", S), ("M19,12H22", S)],
  'ic_map': [("M9,4L3,6V20L9,18L15,20L21,18V4L15,6Z", S), ("M9,4V18", S), ("M15,6V20", S)],
  'ic_walk': [(circle(13.5, 4, 2), S), ("M12.5,8L11,14", S), ("M11,14L8,21", S), ("M11,14L14.5,17L15,21", S), ("M12.5,8L15,11L18,12", S), ("M12.5,8L9,10.5L8,13.5", S)],
  'ic_schedule': [(circle(12, 12, 9), S), ("M12,7V12L15.5,14", S)],
  'ic_event': [("M5,5H19A2,2 0 0,1 21,7V19A2,2 0 0,1 19,21H5A2,2 0 0,1 3,19V7A2,2 0 0,1 5,5Z", S), ("M3,10H21", S), ("M8,3V7", S), ("M16,3V7", S)],
  'ic_history': [("M3.5,12A8.5,8.5 0 1,0 6,6", S), ("M3,3.5V8H7.5", S), ("M12,8V12L15,14", S)],
  # Social e mensagens
  'ic_chat': [("M5,4H19A2,2 0 0,1 21,6V15A2,2 0 0,1 19,17H11L6,21V17H5A2,2 0 0,1 3,15V6A2,2 0 0,1 5,4Z", S)],
  'ic_comment': [("M12,3.5A8.5,8.5 0 1,1 7.7,19.3L3.5,20.5L4.7,16.3A8.5,8.5 0 0,1 12,3.5Z", S)],
  'ic_send': [("M21,3L3,10.5L10,13L12.5,20Z", S), ("M21,3L10,13", S)],
  'ic_share': [(circle(18, 5.5, 2.5), S), (circle(6, 12, 2.5), S), (circle(18, 18.5, 2.5), S), ("M8.2,10.8L15.8,6.7", S), ("M8.2,13.2L15.8,17.3", S)],
  'ic_inbox': [("M12,3V11", S), ("M8.5,7.5L12,11L15.5,7.5", S), ("M3,13H8L9.5,16H14.5L16,13H21V19A2,2 0 0,1 19,21H5A2,2 0 0,1 3,19Z", S)],
  'ic_thumb_up': [(THUMB[0], S), (THUMB[1], S)],
  'ic_notifications': [(BELL, S), ("M10,21H14", S)],
  'ic_notifications_off': [("M8,6.6A6,6 0 0,1 18,11V14", S), ("M17,18H4.5L6,16.5V11C6,10.2 6.2,9.5 6.4,8.8", S), ("M10,21H14", S), ("M3,3L21,21", S)],
  'ic_new': [("M12,3L13.8,9.2L20,11L13.8,12.8L12,19L10.2,12.8L4,11L10.2,9.2Z", S), ("M19,3V6", S), ("M17.5,4.5H20.5", S)],
  'ic_lightbulb': [("M12,3A6,6 0 0,0 8.5,13.9C9.1,14.4 9.5,15.1 9.5,15.9V16H14.5V15.9C14.5,15.1 14.9,14.4 15.5,13.9A6,6 0 0,0 12,3Z", S), ("M9.5,19H14.5", S), ("M10.5,21.5H13.5", S)],
  'ic_celebration': [("M4,20.5L8,9L15,16Z", S), ("M14,3V5", S), ("M19,8H21", S), ("M17,4L16,6", S), ("M20.5,11L18.5,12", S)],
  # Estado
  'ic_check_circle': [(circle(12, 12, 9), S), ("M8,12.5L10.8,15.2L16,9.5", S)],
  'ic_check_small': [("M5,12.5L9.5,17L19,7.5", S)],
  'ic_info': [(circle(12, 12, 9), S), ("M12,11V16.5", S), dot(12, 7.8)],
  'ic_error': [(circle(12, 12, 9), S), ("M12,7.5V13", S), dot(12, 16.2)],
  'ic_star_border': [(STAR, S)],
  'ic_star': [(STAR, 'fillstroke')],
  'ic_verified': [("M12,3L19.5,6V12C19.5,16.3 16.4,19.9 12,21C7.6,19.9 4.5,16.3 4.5,12V6Z", S), ("M8.5,12L11,14.5L15.5,9.5", S)],
  'ic_badge': [(circle(12, 9, 6), S), ("M8.5,13.9L7,22L12,19L17,22L15.5,13.9", S)],
  'ic_lock': [("M7,11H17A2,2 0 0,1 19,13V19A2,2 0 0,1 17,21H7A2,2 0 0,1 5,19V13A2,2 0 0,1 7,11Z", S), ("M8,11V7A4,4 0 0,1 16,7V11", S)],
  'ic_visibility': [("M2,12C2,12 5.5,5 12,5C18.5,5 22,12 22,12C22,12 18.5,19 12,19C5.5,19 2,12 2,12Z", S), (circle(12, 12, 3), S)],
  'ic_wifi_off': [("M2,8.8A15,15 0 0,1 8,5.3", S), ("M12.5,5A15,15 0 0,1 22,8.8", S), ("M5,12.3A10,10 0 0,1 10,9.6", S), ("M16,10.8A10,10 0 0,1 19,12.3", S), ("M8.5,15.8A5,5 0 0,1 15.5,15.8", S), dot(12, 19.5), ("M3,3L21,21", S)],
  'ic_gavel': [("M13,3L20,10L17,13L10,6Z", S), ("M11.5,9.5L3.5,17.5L6.5,20.5L14.5,12.5", S), ("M13,21H21", S)],
  # Mídia e edição
  'ic_camera': [("M5,7H8L9.5,4.5H14.5L16,7H19A2,2 0 0,1 21,9V18A2,2 0 0,1 19,20H5A2,2 0 0,1 3,18V9A2,2 0 0,1 5,7Z", S), (circle(12, 13.5, 3.5), S)],
  'ic_gallery': [("M5,4H19A2,2 0 0,1 21,6V18A2,2 0 0,1 19,20H5A2,2 0 0,1 3,18V6A2,2 0 0,1 5,4Z", S), (circle(8.5, 9.5, 1.5), S), ("M21,15.5L16,10.5L6.5,20", S)],
  'ic_edit': [("M4,20H8L19,9L15,5L4,16Z", S), ("M13,7L17,11", S)],
  'ic_delete': [("M4,7H20", S), ("M10,3.5H14", S), ("M6,7L7,20H17L18,7", S), ("M10,11V16.5", S), ("M14,11V16.5", S)],
}
ICONS['ic_walk_small'] = ICONS['ic_walk']

FAMILIES = [
 ("Navegação", ['ic_home','ic_home_filled','ic_swap','ic_forum','ic_forum_filled','ic_favorite_border','ic_favorite','ic_person','ic_person_filled','ic_back','ic_chevron_right','ic_close','ic_add','ic_search','ic_explore','ic_settings','ic_tune','ic_logout']),
 ("Livros e leitura", ['ic_book','ic_library','ic_local_library','ic_bookmark_border','ic_bookmark','ic_school','ic_donate']),
 ("Encontro e lugar", ['ic_place','ic_location_off','ic_my_location','ic_map','ic_walk','ic_schedule','ic_event','ic_history']),
 ("Social e mensagens", ['ic_chat','ic_comment','ic_send','ic_share','ic_inbox','ic_thumb_up','ic_notifications','ic_notifications_off','ic_new','ic_lightbulb','ic_celebration']),
 ("Estado", ['ic_check_circle','ic_check_small','ic_info','ic_error','ic_star_border','ic_star','ic_verified','ic_badge','ic_lock','ic_visibility','ic_wifi_off','ic_gavel']),
 ("Mídia e edição", ['ic_camera','ic_gallery','ic_edit','ic_delete']),
]

def android(name, paths):
    path = os.path.join(OUT, name + '.xml')
    old = open(path).read() if os.path.exists(path) else ''
    w = re.search(r'android:width="([^"]+)"', old)
    tint = re.search(r'android:tint="([^"]+)"', old)
    w = w.group(1) if w else '24dp'
    lines = ['<?xml version="1.0" encoding="utf-8"?>',
             '<!-- Ícone LeiaUFMS (docs/09-icones.md): grade 24, traço 2, pontas e junções redondas -->',
             '<vector xmlns:android="http://schemas.android.com/apk/res/android"',
             f'    android:width="{w}"', f'    android:height="{w}"',
             '    android:viewportWidth="24"', '    android:viewportHeight="24"']
    lines.append(f'    android:tint="{tint.group(1) if tint else "?attr/colorOnSurface"}"')
    lines[-1] += '>'
    for d, mode in paths:
        lines.append('    <path')
        if mode in (F, 'fillstroke'):
            lines.append('        android:fillColor="@android:color/white"')
        if mode in (S, 'fillstroke'):
            lines += ['        android:strokeColor="@android:color/white"', '        android:strokeWidth="2"',
                      '        android:strokeLineCap="round"', '        android:strokeLineJoin="round"']
        lines.append(f'        android:pathData="{d}" />')
    lines.append('</vector>\n')
    open(path, 'w').write('\n'.join(lines))

def svg(paths, size, color):
    out = [f'<svg xmlns="http://www.w3.org/2000/svg" width="{size}" height="{size}" viewBox="0 0 24 24">']
    for d, mode in paths:
        fill = color if mode in (F, 'fillstroke') else 'none'
        stroke = color if mode in (S, 'fillstroke') else 'none'
        out.append(f'<path d="{d}" fill="{fill}" stroke="{stroke}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>')
    out.append('</svg>')
    return ''.join(out)

for n, p in ICONS.items():
    android(n, p)

html = ['<!doctype html><meta charset=utf-8><title>Ícones LeiaUFMS</title><style>body{font:13px Inter,system-ui;margin:24px}'
        '.t{display:flex;gap:24px}.pane{padding:16px;border-radius:12px}.l{background:#fff;color:#111827}.d{background:#0E1116;color:#F3F4F6}'
        'h2{font-size:14px;margin:16px 0 8px}.g{display:grid;grid-template-columns:repeat(6,120px);gap:8px}.c{display:flex;flex-direction:column;align-items:center;gap:4px;padding:8px}'
        '.s{display:flex;gap:10px;align-items:end}small{font-size:10px;opacity:.7}</style><div class=t>']
for cls, color in (('l', '#1B4F8F'), ('d', '#8DB8F2')):
    html.append(f'<div class="pane {cls}">')
    for fam, names in FAMILIES:
        html.append(f'<h2>{fam}</h2><div class=g>')
        for n in names:
            html.append(f'<div class=c><div class=s>{svg(ICONS[n],16,color)}{svg(ICONS[n],24,color)}{svg(ICONS[n],48,color)}</div><small>{n[3:]}</small></div>')
        html.append('</div>')
    html.append('</div>')
html.append('</div>')
open(SHEET, 'w').write(''.join(html))
print(len(ICONS), 'ícones')

# Fontes SVG (uma por ícone, cor atual) para o handoff
if len(sys.argv) > 3:
    os.makedirs(sys.argv[3], exist_ok=True)
    for n, p in ICONS.items():
        open(os.path.join(sys.argv[3], n[3:] + '.svg'), 'w').write(svg(p, 24, 'currentColor'))
