"""Structural regression checks; not a substitute for a rendered client test."""
import json
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]

class GlintWiring(unittest.TestCase):
    def test_overlay_is_client_only(self):
        config = json.loads((ROOT / 'src/main/resources/aero_enchant_compat.mixins.json').read_text())
        self.assertIn('client.BlockEntityRenderDispatcherMixin', config.get('client', []))
        self.assertNotIn('client.BlockEntityRenderDispatcherMixin', config['mixins'])

    def test_overlay_keeps_encased_renderer_and_model_data(self):
        path = ROOT / 'src/main/java/dev/danelord/createaero/client/PlacedBlockGlint.java'
        self.assertTrue(path.exists(), 'Missing additive placed-block glint path')
        source = path.read_text()
        for required in ['Compatibility.shouldGlint', 'getRenderGlint().get()',
                         'SheetedDecalTextureGenerator', 'CustomRenderType.getGLINT()',
                         'renderBatched', 'getModelData()', 'pushPose()', 'popPose()']:
            self.assertIn(required, source)
        self.assertNotIn('registerBlockEntityRenderer', source)
        self.assertNotIn('AllPartialModels', source)

    def test_flywheel_keeps_world_overlay_pass(self):
        config = json.loads((ROOT / 'src/main/resources/aero_enchant_compat.mixins.json').read_text())
        self.assertIn('client.SimpleBlockEntityVisualizerMixin', config.get('client', []))
        path = ROOT / 'src/main/java/dev/danelord/createaero/mixin/client/SimpleBlockEntityVisualizerMixin.java'
        self.assertTrue(path.exists())
        source = path.read_text()
        self.assertIn('skipVanillaRender', source)
        self.assertIn('Compatibility.supports', source)
        self.assertIn('setReturnValue(false)', source)
        self.assertNotIn('createVisual', source)
        self.assertNotIn('CemConfig', source)

    def test_injection_runs_after_original_world_renderer(self):
        path = ROOT / 'src/main/java/dev/danelord/createaero/mixin/client/BlockEntityRenderDispatcherMixin.java'
        self.assertTrue(path.exists(), 'Missing world dispatcher overlay hook')
        source = path.read_text()
        self.assertIn('setupAndRender', source)
        self.assertIn('@At("TAIL")', source)
        self.assertNotIn('cancellable', source)
        self.assertNotIn('renderItem', source)

if __name__ == '__main__':
    unittest.main()
