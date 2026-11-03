import math
C30=math.cos(math.radians(30))
ROT=[0.0]
def P(x,y,z,s,ox,oy):
    c,si=math.cos(ROT[0]),math.sin(ROT[0]); x,y=x*c-y*si, x*si+y*c
    return (ox+(x-y)*C30*s, oy+(x+y)*0.5*s - z*s)
def poly(pts): return "M"+" L".join(f"{a:.1f},{b:.1f}" for a,b in pts)+"Z"
def build(heights, s, side, step, fg, bg, ox=128, oy=150, book=True, shade=(1.0,0.62,0.85), BW=5.2, BD=3.0, BL=0.9):
    out=[]
    def face(pts,op): out.append(f'<path fill="{fg}" fill-opacity="{op}" d="{poly(pts)}"/>')
    def sepface(pts): out.append(f'<path fill="{bg}" d="{poly(pts)}"/>')
    saved=ROT[0]; ROT[0]=0.0
    if book:
        W=BW; D=BD; lift=BL; t=0.4
        # página esquerda (-x) e direita (+x), lombada em x=0 ao longo de y
        for sgn in (-1,1):
            top=[P(0.25*sgn,-D,t,s,ox,oy),P(W*sgn,-D,t+lift,s,ox,oy),P(W*sgn,D,t+lift,s,ox,oy),P(0.25*sgn,D,t,s,ox,oy)]
            face(top,shade[0])
        # espessura (bloco de páginas) na frente: borda y=+D
        for sgn in (-1,1):
            edge=[P(0.25*sgn,D,t,s,ox,oy),P(W*sgn,D,t+lift,s,ox,oy),P(W*sgn,D,lift*0.4,s,ox,oy),P(0.25*sgn,D,0,s,ox,oy)]
            face(edge,shade[1])
    ROT[0]=saved
    cols=[(0,0,heights[0])]
    for i,h in enumerate(heights[1:],1):
        for dx,dy in [(1,0),(-1,0),(0,1),(0,-1)]: cols.append((dx*i*step,dy*i*step,h))
    a=side/2; z0=0.9 if book else 0
    def depth(c):
        cc,si=math.cos(ROT[0]),math.sin(ROT[0]); x,y=c[0]*cc-c[1]*si, c[0]*si+c[1]*cc
        return (x+y, x)
    for x,y,h in sorted(cols,key=depth):
        Lf=[P(x-a,y+a,z0,s,ox,oy),P(x+a,y+a,z0,s,ox,oy),P(x+a,y+a,z0+h,s,ox,oy),P(x-a,y+a,z0+h,s,ox,oy)]
        Rf=[P(x+a,y+a,z0,s,ox,oy),P(x+a,y-a,z0,s,ox,oy),P(x+a,y-a,z0+h,s,ox,oy),P(x+a,y+a,z0+h,s,ox,oy)]
        Tf=[P(x-a,y-a,z0+h,s,ox,oy),P(x+a,y-a,z0+h,s,ox,oy),P(x+a,y+a,z0+h,s,ox,oy),P(x-a,y+a,z0+h,s,ox,oy)]
        # contorno de separação do fundo (silhueta um pouco maior) para destacar das colunas de trás
        sil=[P(x-a,y+a,z0,s,ox,oy),P(x+a,y+a,z0,s,ox,oy),P(x+a,y-a,z0,s,ox,oy),P(x+a,y-a,z0+h,s,ox,oy),P(x-a,y-a,z0+h,s,ox,oy),P(x-a,y+a,z0+h,s,ox,oy)]
        out.append(f'<path fill="{bg}" stroke="{bg}" stroke-width="3" stroke-linejoin="round" d="{poly(sil)}"/>')
        face(Lf,shade[0]); face(Rf,shade[1]); face(Tf,shade[2])
    return "".join(out)
