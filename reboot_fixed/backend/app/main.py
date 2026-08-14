import os, json
from datetime import datetime, timedelta
from typing import Optional
import httpx
from fastapi import FastAPI, Depends, HTTPException, Header
from pydantic import BaseModel, EmailStr
from jose import jwt

app=FastAPI(title="REBOOT API",version="1.0.0")
JWT_SECRET=os.getenv("JWT_SECRET","dev-only-secret")
GROQ_KEY=os.getenv("GROQ_API_KEY","")
GROQ_MODEL=os.getenv("GROQ_MODEL","openai/gpt-oss-20b")
USERS={}
HISTORY={}

class AuthIn(BaseModel):
    email: EmailStr
    password: str
    name: Optional[str]=""

class Onboarding(BaseModel):
    name: str
    habits:list[str]=[]
    problems:list[str]=[]
    goals:list[str]=[]

class ChatIn(BaseModel):
    message:str
    mode:str="Друг"
    history:list[dict]=[]

def token(email):
    return jwt.encode({"sub":email,"exp":datetime.utcnow()+timedelta(days=30)},JWT_SECRET,algorithm="HS256")

def current(authorization:Optional[str]=Header(None)):
    if not authorization or not authorization.startswith("Bearer "):
        raise HTTPException(401,"Authentication required")
    try:
        data=jwt.decode(authorization[7:],JWT_SECRET,algorithms=["HS256"])
        return data["sub"]
    except Exception: raise HTTPException(401,"Invalid token")

@app.get("/health")
def health(): return {"ok":True,"service":"reboot-api"}

@app.post("/api/v1/auth/register")
def register(x:AuthIn):
    if x.email in USERS: raise HTTPException(409,"Email already registered")
    USERS[x.email]={"email":x.email,"name":x.name or "User","habits":[],"problems":[],"goals":[]}
    HISTORY[x.email]=[]
    return {"access_token":token(x.email),"user":USERS[x.email]}

@app.post("/api/v1/auth/login")
def login(x:AuthIn):
    if x.email not in USERS: raise HTTPException(401,"Invalid credentials")
    return {"access_token":token(x.email),"user":USERS[x.email]}

@app.get("/api/v1/profile")
def profile(email=Depends(current)): return USERS[email]

@app.put("/api/v1/profile/onboarding")
def onboarding(x:Onboarding,email=Depends(current)):
    USERS[email].update(x.model_dump())
    return USERS[email]

@app.get("/api/v1/history")
def history(email=Depends(current)): return {"items":HISTORY.get(email,[])}

SYSTEM="""Ты REBOOT — безопасный персональный ИИ-наставник.
Адаптируй ответы под профиль пользователя, его привычки, проблемы и цели.
Режимы: Друг — поддержка; Тренер — конкретика; Провокатор — бросает вызов без оскорблений;
Мудрец — объясняет; Турбо — энергичный, но безопасный.
Всегда предлагай маленький реалистичный следующий шаг.
Не диагностируй болезни. Не давай опасных инструкций. Не стыди пользователя.
Для JSON-задач возвращай валидный JSON без markdown.
"""

@app.post("/api/v1/ai/chat")
async def chat(x:ChatIn,email=Depends(current)):
    profile=USERS[email]
    prompt=f"""Профиль: {json.dumps(profile,ensure_ascii=False)}
Режим: {x.mode}
Сообщение: {x.message}
История: {json.dumps(x.history[-5:],ensure_ascii=False)}
Ответь на языке сообщения."""
    if not GROQ_KEY:
        answer="Backend работает, но GROQ_API_KEY ещё не задан. Добавь его в backend/.env."
    else:
        payload={"model":GROQ_MODEL,"messages":[{"role":"system","content":SYSTEM},{"role":"user","content":prompt}],"temperature":0.7}
        async with httpx.AsyncClient(timeout=60) as c:
            r=await c.post("https://api.groq.com/openai/v1/chat/completions",
                headers={"Authorization":f"Bearer {GROQ_KEY}","Content-Type":"application/json"},json=payload)
            if r.status_code>=400: raise HTTPException(502,r.text)
            answer=r.json()["choices"][0]["message"]["content"]
    item={"time":datetime.utcnow().isoformat(),"mode":x.mode,"user":x.message,"assistant":answer}
    HISTORY.setdefault(email,[]).append(item)
    return {"answer":answer,"item":item}

@app.post("/api/v1/ai/plan")
async def plan(email=Depends(current)):
    p=USERS[email]
    if not GROQ_KEY: return {"plan":{"title":"Демо-план","steps":["Маленький шаг на сегодня","Проверить прогресс","Повторить завтра"]}}
    prompt=f"""Профиль {json.dumps(p,ensure_ascii=False)}
Сделай безопасный 7-дневный план. Верни JSON:
{{"title":"...","steps":[{{"day":1,"task":"...","minutes":5}}]}}"""
    payload={"model":GROQ_MODEL,"messages":[{"role":"system","content":SYSTEM},{"role":"user","content":prompt}],"temperature":0.3}
    async with httpx.AsyncClient(timeout=60) as c:
        r=await c.post("https://api.groq.com/openai/v1/chat/completions",
            headers={"Authorization":f"Bearer {GROQ_KEY}","Content-Type":"application/json"},json=payload)
    return {"raw":r.json()["choices"][0]["message"]["content"]}

@app.get("/api/v1/dashboard")
def dashboard(email=Depends(current)): return {"level":1,"xp":0,"streak":0,"today_task":"Сделай маленький полезный шаг"}

@app.get("/api/v1/plans")
def plans(email=Depends(current)): return {"items":[{"type":x,"title":x} for x in ["Тренировки","Саморазвитие","Карьера","Психология","Здоровье","Свободная цель"]]}

@app.get("/api/v1/progress")
def progress(email=Depends(current)): return {"xp":0,"level":1,"streak":0,"achievements":[]}

@app.get("/api/v1/achievements")
def achievements(email=Depends(current)): return {"items":[]}

@app.get("/api/v1/habits")
def habits(email=Depends(current)): return {"items":USERS[email]["habits"]}

@app.get("/api/v1/settings")
def settings(email=Depends(current)): return {"language":"ru","voice":"neutral","strictness":5}

@app.get("/api/v1/subscription")
def subscription(email=Depends(current)): return {"plan":"free","status":"active"}

@app.get("/api/v1/referral")
def referral(email=Depends(current)): return {"code":"REBOOT-"+email.split("@")[0][:8]}
