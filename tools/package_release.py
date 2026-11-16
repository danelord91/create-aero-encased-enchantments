#!/usr/bin/env python3
"""Package the tested local 0.3.0 binary and reproducible source; never publishes."""
import hashlib
import json
from pathlib import Path
import shutil
import subprocess
import zipfile

root = Path(__file__).resolve().parents[1]
dist = root / 'dist'
dist.mkdir(exist_ok=True)
jar = root / 'build/libs/create-aero-enchantment-compat-0.3.0.jar'
target = dist / jar.name
shutil.copyfile(jar, target)
source = dist / 'create-aero-enchantment-compat-0.3.0-source.zip'
files = subprocess.check_output(['git', 'ls-files', '-co', '--exclude-standard', '-z'], cwd=root).decode().split('\0')
files = sorted({p for p in files if p and p not in {'HANDOFF.md', 'PROGRESS.md'} and not p.startswith(('dist/', 'evidence/', 'libs/'))})
with zipfile.ZipFile(source, 'w', compression=zipfile.ZIP_DEFLATED, compresslevel=9) as out:
    for name in files:
        path = root / name
        if not path.is_file():
            raise RuntimeError(f'Missing source file: {name}')
        if path.suffix == '.jar' and name != 'gradle/wrapper/gradle-wrapper.jar':
            raise RuntimeError(f'Unexpected binary: {name}')
        info = zipfile.ZipInfo('create-aero-enchantment-compat-0.3.0/' + name, (1980, 1, 1, 0, 0, 0))
        info.compress_type = zipfile.ZIP_DEFLATED
        info.external_attr = ((0o100755 if path.stat().st_mode & 0o111 else 0o100644) << 16)
        out.writestr(info, path.read_bytes(), compresslevel=9)

def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

logs = ['final-both-dev', 'production-both', 'production-encased', 'production-dnd', 'production-neither', 'static-final']
verification = {
    'version': '0.3.0',
    'artifacts': {target.name: sha(target), source.name: sha(source)},
    'inputs': {k: v['sha256'] for k, v in json.loads((root / 'input-manifest.json').read_text()).items()},
    'runtime_counts': {'development_both': 29, 'production_both': 29, 'production_encased_only': 29, 'production_dnd_only': 7, 'production_neither': 7},
    'evidence_sha256': {f'evidence/{n}.log': sha(root / 'evidence' / f'{n}.log') for n in logs},
    'limitations': ['No 0.3.0 client startup or pixel validation', 'No full modpack, live world or powered moving-contraption validation', 'Newer dependency binaries were not tested'],
}
(dist / 'verification.json').write_text(json.dumps(verification, indent=2) + '\n')
print(json.dumps(verification, indent=2))
