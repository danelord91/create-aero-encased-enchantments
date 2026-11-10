#!/usr/bin/env python3
"""Inventory actual installed CEM and Encased resource tags, not filename guesses."""
import collections, hashlib, json, re, zipfile
from pathlib import Path
root = Path(__file__).resolve().parents[1]
with zipfile.ZipFile(root/'libs/cem.jar') as cem, zipfile.ZipFile(root/'libs/encased.jar') as enc:
    assert cem.testzip() is None and enc.testzip() is None
    def values(z, name):
        return json.loads(z.read(name))['values']
    mining = values(cem, 'data/minecraft/tags/item/enchantable/mining.json')
    loot = values(cem, 'data/minecraft/tags/item/enchantable/mining_loot.json')
    mapped = values(cem, 'data/createenchantablemachinery/tags/item/enchantable_blocks.json')
    assert len(mapped) == 11 and len(set(mapped)) == 11 and set(mapped) == set(mining)
    families = {'mixers':'mechanical_mixer','presses':'mechanical_press','encased_fans':'encased_fan','spouts':'spout','drills':'mechanical_drill','saws':'mechanical_saw','harvesters':'mechanical_harvester','ploughs':'mechanical_plough','rollers':'mechanical_roller'}
    items=[]
    for family, original in families.items():
        upstream='create:'+original
        assert upstream in mapped
        entries=values(enc, f'data/create/tags/item/{family}.json')
        assert entries.count(upstream)==1
        variants=[x for x in entries if x.startswith('createcasing:')]
        assert len(variants)==(3 if family=='spouts' else 9)
        for item in variants:
            loot_effects = ['fortune','silk_touch'] if upstream in loot else []
            efficiency_effect = family != 'harvesters'
            items.append({'item':item,'family':family,'cem_counterpart':upstream,
                'upstream_eligible_enchantments':['efficiency']+loot_effects,
                'upstream_effects':({'efficiency':'processing or block-breaking speed'} if efficiency_effect else {'efficiency':'upstream tag eligibility; no efficiency handler found in harvester actor'}) | {x:'enchanted tool loot, subject to target loot table' for x in loot_effects},
                'addon_eligible_enchantments':(['efficiency'] if efficiency_effect else []) + loot_effects,
                'verification_state':'production-JAR GameTests: anvil, placement, entity save/reload, drop, pick, wrench, unenchanted drop and glint policy; actor variants additionally controlled capture/remove/disassembly; effects tested on representative family fixtures',
                'exclusion_reason':'Efficiency intentionally not offered: no harvester speed effect in upstream actor' if not efficiency_effect else None})
    assert len(items)==75 and len({x['item'] for x in items})==75
    counts=dict(sorted(collections.Counter(x['family'] for x in items).items()))
    result={'inputs_sha256':{x:hashlib.sha256((root/'libs'/f'{x}.jar').read_bytes()).hexdigest() for x in ('cem','encased','create')},
       'method':'Installed JAR item tags cross-checked against upstream registration source; tag eligibility does not establish functional compatibility.',
       'cem_registered_items':mapped,'cem_mining_loot_tag':loot,'counts_by_family':counts,'total_relevant_encased_items':len(items),'items':items,
       'no_encased_counterpart_in_installed_family_tags':['create:millstone','create:crushing_wheel'],
       'excluded_other_items_reason':'No corresponding CEM registered machinery/effect; e.g. shafts, gears and deployers are not added by this inventory.',
       'limitations':['Resource tags inventory built-in variants; it does not prove dynamically registered third-party casing variants.', 'Enchantment effects are conditioned by applicable loot tables and upstream mechanics.', 'Harvester Efficiency is eligible upstream but deliberately not offered by this addon because no speed handler was located.', 'Representative effects and controlled single-block contraption fixtures are not exhaustive powered-contraption, every loot-table, client pixel or full-pack verification.']}
    (root/'coverage.json').write_text(json.dumps(result,indent=2)+'\n')
    print(json.dumps({'total':len(items),'counts':counts,'mapped':len(mapped)},indent=2))
