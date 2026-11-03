"""Paliteiro em 3D (colunas quadradas em 4 eixos, topo reto) sobre um livro aberto, em isométrica."""
import math
C30=math.cos(math.radians(30))
PTS=[]
def proj(x,y,z,rot):
    c,s=math.cos(rot),math.sin(rot); x,y=x*c-y*s, x*s+y*c
    return ((x-y)*C30, (x+y)*0.5 - z)
def poly(pts):
    PTS.extend(pts); return "M"+" L".join(f"{a:.2f},{b:.2f}" for a,b in pts)+"Z"
def scene(n_arm=4, h=18, side=0.7, step=1.55, rot=30, W=10, D=6, lift=2.2, thick=1.2, fg="#fff", bg="#1B4F8F", shade=(1.0,0.62,0.85)):
    r=math.radians(rot); out=[]
    def face(pts,op,rr): out.append((op, [proj(*p,rr) for p in pts]))
    # livro: páginas curvas (sobem da lombada e achatam), bloco de folhas na frente
    N=14
    for sgn in (-1,1):
        xs=[sgn*(0.35+(W-0.35)*i/N) for i in range(N+1)]
        z=lambda x: thick + lift*math.sin(math.pi/2*abs(x)/W)
        back=[(x,-D,z(x)) for x in xs]; front=[(x,D,z(x)) for x in reversed(xs)]
        face(back+front, shade[0], 0)
        edge=[(x,D,z(x)) for x in xs]+[(x,D,z(x)-thick) for x in reversed(xs)]
        face(edge, shade[1], 0)
    cols=[(0,0)]
    for i in range(1,n_arm+1):
        for dx,dy in [(1,0),(-1,0),(0,1),(0,-1)]: cols.append((dx*i*step,dy*i*step))
    def depth(c):
        cc,s=math.cos(r),math.sin(r); x,y=c[0]*cc-c[1]*s, c[0]*s+c[1]*cc
        return (x+y, x)
    a=side/2; z0=thick+0.2
    for x,y in sorted(cols,key=depth):
        # faces visíveis depois do giro: calcula pelos cantos
        Lf=[(x-a,y+a,z0),(x+a,y+a,z0),(x+a,y+a,z0+h),(x-a,y+a,z0+h)]
        Rf=[(x+a,y+a,z0),(x+a,y-a,z0),(x+a,y-a,z0+h),(x+a,y+a,z0+h)]
        Tf=[(x-a,y-a,z0+h),(x+a,y-a,z0+h),(x+a,y+a,z0+h),(x-a,y+a,z0+h)]
        sil=[(x-a,y+a,z0),(x+a,y+a,z0),(x+a,y-a,z0),(x+a,y-a,z0+h),(x-a,y-a,z0+h),(x-a,y+a,z0+h)]
        out.append(('sep',[proj(*p,r) for p in sil]))
        face(Lf,shade[0],r); face(Rf,shade[1],r); face(Tf,shade[2],r)
    return out
def render(items, size=256, pad=0.12, fg="#fff", bg="#1B4F8F", tile=True, rx=56):
    xs=[p[0] for _,pts in items for p in pts]; ys=[p[1] for _,pts in items for p in pts]
    w=max(xs)-min(xs); hh=max(ys)-min(ys); s=size*(1-2*pad)/max(w,hh)
    ox=size/2-(min(xs)+w/2)*s; oy=size/2-(min(ys)+hh/2)*s
    body=[]
    for op,pts in items:
        d="M"+" L".join(f"{ox+a*s:.1f},{oy+b*s:.1f}" for a,b in pts)+"Z"
        if op=='sep': body.append(f'<path fill="{bg}" stroke="{bg}" stroke-width="{max(1.5,size/90):.1f}" stroke-linejoin="round" d="{d}"/>')
        else: body.append(f'<path fill="{fg}" fill-opacity="{op}" d="{d}"/>')
    back=f'<rect width="{size}" height="{size}" rx="{rx}" fill="{bg}"/>' if tile else ''
    return f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {size} {size}" width="{size}" height="{size}"><title>LeiaUFMS</title>{back}{"".join(body)}</svg>'
