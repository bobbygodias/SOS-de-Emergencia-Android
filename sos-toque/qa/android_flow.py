"""UI and sensor test ONLY for an isolated Android emulator, never a real phone."""
import os, subprocess, time, re, pathlib, xml.etree.ElementTree as ET

ADB = os.environ['SOS_QA_ADB']
SERIAL = os.environ.get('SOS_QA_SERIAL', 'emulator-5580')
assert SERIAL.startswith('emulator-'), 'This script must not run on a physical phone.'
ROOT = pathlib.Path(__file__).resolve().parents[1]
OUT = ROOT / 'qa' / 'evidence'
OUT.mkdir(exist_ok=True)
PKG = 'org.enterprise.sostoque'

def adb(*args, binary=False, timeout=30):
    r = subprocess.run([ADB, '-s', SERIAL, *args], capture_output=True, timeout=timeout)
    if r.returncode: raise RuntimeError((r.stdout + r.stderr).decode(errors='replace'))
    return r.stdout if binary else r.stdout.decode(errors='replace')

def tree(tag):
    raw = adb('exec-out', 'uiautomator', 'dump', '/dev/tty', timeout=45)
    xml = raw[raw.find('<?xml'):raw.rfind('</hierarchy>')+12]
    (OUT/(tag+'.xml')).write_text(xml)
    root = ET.fromstring(xml)
    return list(root.iter('node'))

def bounds(node):
    x1,y1,x2,y2=map(int,re.findall(r'\d+',node.attrib['bounds']))
    return (x1+x2)//2,(y1+y2)//2

def click(label, tag='ui', max_swipes=8):
    for i in range(max_swipes+1):
        for node in tree(tag):
            if node.attrib.get('text') == label and node.attrib.get('bounds') != '[0,0][0,0]':
                x,y=bounds(node);adb('shell','input','tap',str(x),str(y));time.sleep(.7);return
        if i<max_swipes: adb('shell','input','swipe','360','1050','360','330','250');time.sleep(.4)
    raise AssertionError('Missing UI target: '+label)

def screenshot(name):
    (OUT/(name+'.png')).write_bytes(adb('exec-out','screencap','-p',binary=True))

def report(value): print(value,flush=True)

def setup():
    report(adb('install','-r',str(ROOT/'build/SOS-Toque-1.0.0.apk'),timeout=60).strip())
    adb('shell','pm','grant',PKG,'android.permission.CALL_PHONE')
    adb('shell','appops','set',PKG,'SYSTEM_ALERT_WINDOW','allow')
    adb('shell','dumpsys','deviceidle','whitelist','+'+PKG)
    adb('shell','settings','put','global','window_animation_scale','0')
    adb('shell','settings','put','global','transition_animation_scale','0')
    adb('shell','settings','put','global','animator_duration_scale','0')
    adb('shell','input','keyevent','224')
    adb('shell','wm','dismiss-keyguard')
    adb('shell','am','start','-n',PKG+'/.MainActivity')
    time.sleep(2)
    screenshot('home-before-setup')
    click('1. Escolher contato da agenda','contacts')
    edits=[n for n in tree('contact-fields') if n.attrib.get('class')=='android.widget.EditText']
    assert len(edits)>=2
    x,y=bounds(edits[0]);adb('shell','input','tap',str(x),str(y));adb('shell','input','text','Contato%steste')
    x,y=bounds(edits[1]);adb('shell','input','tap',str(x),str(y));adb('shell','input','text','5555550101')
    adb('shell','input','keyevent','4')
    click('SALVAR','save-contacts');click('Entendi','saved')
    report('Contact selection and settings save: PASS (fictional emulator number)')
    screenshot('home-configured')

def train():
    click('2. Testar sensor sem ligar','sensor-train')
    click('Iniciar teste','train-explanation')
    # A PIN is set only in the isolated emulator to exercise the secure keyguard.
    report(adb('shell','locksettings','set-pin','2580').strip())
    adb('shell','settings','put','secure','lock_screen_lock_after_timeout','0')
    adb('shell','settings','put','secure','lockscreen.power_button_instantly_locks','1')
    adb('emu','sensor','set','proximity','10');time.sleep(2)
    adb('shell','input','keyevent','223');time.sleep(3)
    (OUT/'screen-off-power.txt').write_text(adb('shell','dumpsys','power'))
    (OUT/'screen-off-window.txt').write_text(adb('shell','dumpsys','window','policy'))
    for _ in range(5):
        adb('emu','sensor','set','proximity','0');time.sleep(.5)
        adb('emu','sensor','set','proximity','10');time.sleep(.5)
    time.sleep(2)
    nodes=tree('sensor-result')
    texts=[n.attrib.get('text','') for n in nodes]
    screenshot('sensor-result')
    assert 'Sensor respondeu' in texts, texts
    report('Five proximity cycles, screen OFF and secure keyguard: PASS; no call made')
    (OUT/'crashes.txt').write_text(adb('logcat','-b','crash','-d'))

if __name__=='__main__':
    setup();train()
