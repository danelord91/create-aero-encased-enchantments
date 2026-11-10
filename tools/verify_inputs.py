#!/usr/bin/env python3
"""Validate exact build inputs without modifying any archives."""
import hashlib
import json
from pathlib import Path
import zipfile

root = Path(__file__).resolve().parents[1]
manifest = json.loads((root / 'input-manifest.json').read_text())
for filename, expected in manifest.items():
    path = root / 'libs' / filename
    if not path.is_file():
        raise SystemExit(f'Missing {path}; copy {expected["original_filename"]} here as {filename}')
    actual = hashlib.sha256(path.read_bytes()).hexdigest()
    if actual != expected['sha256']:
        raise SystemExit(f'Wrong binary for {filename}: SHA-256 {actual}')
    with zipfile.ZipFile(path) as archive:
        bad = archive.testzip()
        if bad:
            raise SystemExit(f'CRC failure: {filename}: {bad}')
    print(f'OK {filename}: {actual}')
print('All three exact input JARs verified.')
