# Building and testing

## Prerequisites

- Java 21 JDK (`java` and `javap` on PATH)
- Python 3
- Internet access for the Gradle wrapper, NeoForge and Maven dependencies on the first build
- The three exact upstream Minecraft mod binaries below, obtained from their authors

The repository does not redistribute those mod JARs or include credentials for downloading them.

Create a `libs` directory at the repository root. Download the Minecraft 1.21.1 NeoForge files, rename them as shown and retain the upstream mods' other required dependencies for gameplay:

| Local filename | Required mod version | Official project |
| --- | --- | --- |
| `libs/create.jar` | Create 6.0.10 | https://www.curseforge.com/minecraft/mc-mods/create |
| `libs/cem.jar` | Create: Enchantable Machinery 3.6.0 | https://www.curseforge.com/minecraft/mc-mods/create-enchantable-machinery |
| `libs/encased.jar` | Create Encased 1.10.0-fix1 | https://www.curseforge.com/minecraft/mc-mods/create-encased |

`input-manifest.json` records SHA-256 and sizes. The `doc_...` prefixes in its original-filename provenance came from the initial supplied files; the required local filenames are the three names above. Local JARs are gitignored.

## Verify and build

From the repository root:

```bash
python3 tools/verify_inputs.py
bash gradlew build
```

The output is `build/libs/create-aero-enchantment-compat-0.2.1.jar`. The release JAR excludes GameTest classes, test fixtures and original dependency binaries. Gradle's ordinary `test` task is not the Minecraft GameTest suite.

On Windows, use `gradlew.bat` instead of `bash gradlew` and `py -3` if Python is installed under that command.

## Regression tests

```bash
python3 -m unittest discover -s tools -p 'test_*glint*.py'
bash gradlew runGameTestServer
bash gradlew -p verification runGameTestServer
```

Expected for 0.2.1: **8 glint tests**, and **22 required Minecraft tests** in each server run. The second Minecraft run loads the compiled production JAR; build the main project first. Inspect the explicit all-required-tests-passed line, not only Gradle's exit code.

Use the included isolated structure fixtures and generated test worlds. Do not aim tests at a live world or treat this setup as authorisation to accept the Minecraft server EULA.

## Optional client loading checks

With a working display and supported OpenGL implementation:

```bash
bash gradlew -p verification runClient
# Quit Minecraft normally after it starts, then:
python3 tools/verify_client_transforms.py
```

The verification client exports Mixin-transformed classes. The script checks 8 expected transformed targets. A title-screen launch validates loading, not visible glint on an assembled contraption. Test enchanted/plain negative controls and the renderGlint toggle in a disposable world, with the graphics backend/shader configurations you intend to support.

## Changing the addon version

Update production `build.gradle` and `src/main/resources/META-INF/neoforge.mods.toml`, plus the exact compiled-JAR path and dependency range in the verification harness. Rerun build, both GameTest paths and client checks before claiming a new binary is verified.

## Verified 0.2.1 artifact

SHA-256:

```text
c5662722221785aa74f98d126428bd37e03d9439348f10ed38650eaf206e6e07
```

The original extracted source archive rebuilt this exact binary in the verification environment. This repository preserves those production sources; README, issue templates and project artwork do not change the runtime implementation.
