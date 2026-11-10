"""Verify actual client Mixin exports; run only after the isolated production-JAR client loads."""
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]
EXPORTS = ROOT / 'verification/run/.mixin.out/class'
checks = {
    'com/simibubi/create/content/contraptions/render/ContraptionEntityRenderer': ['aero$carriedCasingGlint', 'ContraptionBlockGlint.render:'],
    'net/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher': ['aero$'],
    'dev/engine_room/flywheel/lib/visualization/SimpleBlockEntityVisualizer': ['aero$'],
}
for family in ['drill', 'saw', 'harvester', 'roller', 'plough']:
    checks[f'io/github/cotrin8672/cem/content/block/{family}/Enchantable{family.title()}MovementBehaviour'] = ['aero$originalRenderer', 'aero$render']
for target, tokens in checks.items():
    path = EXPORTS / (target + '.class')
    assert path.is_file(), f'No actual client transformation export: {target}'
    text = subprocess.check_output(['javap', '-p', '-c', str(path)], text=True)
    for token in tokens:
        assert token in text, f'Missing {token} in real transformed class {target}'
    print('PASS real client transform:', target)
print(f'{len(checks)}/{len(checks)} actual client transformed targets verified (not a pixel test)')
