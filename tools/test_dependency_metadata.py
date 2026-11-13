"""Regression coverage for the 0.2.2 dependency-metadata-only update."""
from pathlib import Path
import json
import os
import shutil
import subprocess
import tempfile
import tomllib
import unittest

ROOT = Path(__file__).resolve().parents[1]


def metadata():
    return tomllib.loads((ROOT / 'src/main/resources/META-INF/neoforge.mods.toml').read_text())


def dependencies():
    return {d['modId']: d for d in metadata()['dependencies']['aero_enchant_compat']}


class DependencyMetadataTests(unittest.TestCase):
    def test_encased_is_an_inclusive_minimum(self):
        self.assertEqual(dependencies()['createcasing']['versionRange'], '[1.10.0-fix1,)')

    def test_create_and_cem_are_inclusive_minimums(self):
        self.assertEqual(dependencies()['create']['versionRange'], '[6.0.10,)')
        self.assertEqual(dependencies()['createenchantablemachinery']['versionRange'], '[3.6.0,)')

    def test_game_loader_and_dependency_flags(self):
        expected = {'minecraft': '[1.21.1]', 'neoforge': '[21.1.265,)'}
        self.assertEqual({k: d['versionRange'] for k, d in dependencies().items() if k in expected}, expected)
        self.assertEqual(set(dependencies()), {'minecraft', 'neoforge', 'create', 'createcasing', 'createenchantablemachinery'})
        self.assertTrue(all(d['side'] == 'BOTH' and d['type'] == 'required' for d in dependencies().values()))

    def test_production_version(self):
        self.assertEqual(metadata()['mods'][0]['version'], '0.2.2')
        self.assertIn("version = '0.2.2'", (ROOT / 'build.gradle').read_text())

    def test_production_jar_harness_version(self):
        harness = tomllib.loads((ROOT / 'verification/src/main/resources/META-INF/neoforge.mods.toml').read_text())
        self.assertEqual(harness['dependencies']['aero_enchant_testharness'][0]['versionRange'], '[0.2.2]')
        self.assertIn('create-aero-enchantment-compat-0.2.2.jar', (ROOT / 'verification/build.gradle').read_text())

    def test_real_maven_range_acceptance(self):
        supplied = os.environ.get('MAVEN_ARTIFACT_JAR')
        candidates = [Path(supplied)] if supplied else list((Path.home() / '.gradle/caches/modules-2/files-2.1/org.apache.maven/maven-artifact').glob('*/*/maven-artifact-*.jar'))
        commons = list((Path.home() / '.gradle/caches/modules-2/files-2.1/org.apache.commons/commons-lang3').glob('*/*/commons-lang3-*.jar'))
        if not candidates or not commons or not shutil.which('java'):
            self.skipTest('Java, Maven artifact and Commons Lang libraries are needed for the actual Maven range check')
        classpath = os.pathsep.join([str(candidates[0]), str(commons[0])])
        source = '''import org.apache.maven.artifact.versioning.*;
class RangeCheck {
 public static void main(String[] args) throws Exception {
  VersionRange range = VersionRange.createFromVersionSpec(args[0]);
  for (int i = 1; i < args.length; i++)
   System.out.println(args[i] + "=" + range.containsVersion(new DefaultArtifactVersion(args[i])));
 }
}'''
        matrices = {
            'createcasing': {'1.9.9': False, '1.10.0': False, '1.10.0-fix1': True, '1.10.0-fix2': True, '1.10.0-fix10': True, '1.10.1': True, '1.11.0': True},
            'create': {'6.0.9': False, '6.0.10': True, '6.0.11': True, '6.1.0': True},
            'createenchantablemachinery': {'3.5.9': False, '3.6.0': True, '3.6.1': True, '3.7.0': True},
            'neoforge': {'21.1.264': False, '21.1.265': True, '21.1.266': True},
        }
        with tempfile.TemporaryDirectory(prefix='create-aero-range-') as temporary:
            path = Path(temporary) / 'RangeCheck.java'
            path.write_text(source)
            for mod, versions in matrices.items():
                with self.subTest(mod=mod):
                    result = subprocess.run(['java', '--class-path', classpath, str(path), dependencies()[mod]['versionRange'], *versions], capture_output=True, text=True)
                    self.assertEqual(result.returncode, 0, result.stderr)
                    actual = {line.split('=')[0]: line.split('=')[1] == 'true' for line in result.stdout.splitlines()}
                    self.assertEqual(actual, versions)


if __name__ == '__main__':
    unittest.main()
