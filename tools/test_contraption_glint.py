"""Contraption overlay wiring and exact pinned bytecode regression checks (not pixels)."""
import json
from pathlib import Path
import subprocess
import unittest

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / 'src/main/java/dev/danelord/createaero'

class ContraptionGlint(unittest.TestCase):
    def test_client_only_additive_hook(self):
        config = json.loads((ROOT / 'src/main/resources/aero_enchant_compat.mixins.json').read_text())
        self.assertIn('client.ContraptionEntityRendererMixin', config['client'])
        self.assertNotIn('client.ContraptionEntityRendererMixin', config['mixins'])
        source = (BASE / 'mixin/client/ContraptionEntityRendererMixin.java').read_text()
        self.assertIn('renderActors(Lnet/minecraft/world/level/Level;', source)
        self.assertIn('@At("HEAD")', source)
        self.assertNotIn('cancellable', source)
        self.assertIn('ContraptionBlockGlint.render', source)

    def test_carried_geometry_selection_and_transform(self):
        source = (BASE / 'client/ContraptionBlockGlint.java').read_text()
        for token in ['getRenderGlint().get()', 'getRenderedBlocks()', 'lookup().apply(pos)',
                      'getBlockEntity(pos)', 'Compatibility.shouldGlintCarried',
                      'matrices.getModelViewProjection()', 'pose.translate(',
                      'getModelData()', 'renderBatched', 'SheetedDecalTextureGenerator',
                      'CustomRenderType.getGLINT()', 'pushPose()', 'popPose()']:
            self.assertIn(token, source)
        for forbidden in ['EnchantableBlockMapping', 'renderInContraption(',
                          'RenderType.solid', 'AllPartialModels', 'supportsVisualization']:
            self.assertNotIn(forbidden, source)

    def test_real_pinned_renderer_runs_hook_in_both_backends(self):
        source = subprocess.check_output(['javap', '-p', '-c', '-s', '-classpath', str(ROOT/'libs/create.jar'),
                  'com.simibubi.create.content.contraptions.render.ContraptionEntityRenderer'], text=True)
        method = source.split('public void render(C,')[1].split('protected static void renderActors')[0]
        self.assertIn('VisualizationManager.supportsVisualization', method)
        self.assertIn('renderActors:', method)
        self.assertLess(method.index('renderActors:'), method.index('ContraptionMatrices.clear:'))
        self.assertIn('(Lnet/minecraft/world/level/Level;Lcom/simibubi/create/foundation/virtualWorld/VirtualRenderWorld;Lcom/simibubi/create/content/contraptions/Contraption;Lcom/simibubi/create/content/contraptions/render/ContraptionMatrices;Lnet/minecraft/client/renderer/MultiBufferSource;)V', source)
        # The world dispatcher hook is NOT used by carried block-entity rendering.
        helper = subprocess.check_output(['javap', '-p', '-c', '-classpath', str(ROOT/'libs/create.jar'),
                  'com.simibubi.create.foundation.render.BlockEntityRenderHelper'], text=True)
        self.assertIn('BlockEntityRenderer.render:', helper)
        self.assertNotIn('setupAndRender:', helper)

    def test_selection_matches_carried_state_and_real_enchantments(self):
        source = (BASE / 'Compatibility.java').read_text()
        self.assertIn('shouldGlintCarried', source)
        self.assertIn('carriedState.getBlock() == be.getBlockState().getBlock()', source)
        self.assertIn('shouldGlint(be, renderGlint)', source)

if __name__ == '__main__':
    unittest.main()
