import json
import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / 'src'))
from parsers.product_content import read_product_content


class ProductContentTest(unittest.TestCase):
    def test_decodes_split_flight_text_with_utf8_byte_length_and_full_specs(self):
        body = '<h2>Mô tả</h2><p>Vải chống nước.</p><p>Đế cao su.</p>'
        stream = '1:' + json.dumps({'product': {'name': 'Pad', 'specs_table': [
            {'rows': [{'label': 'Kích thước', 'value': '900 x 400 x 5 mm'}, {'label': 'Bo viền', 'value': 'Có'}]}
        ]}, 'descriptionBody': '$2'}, ensure_ascii=False) + '\n2:T' + format(len(body.encode('utf-8')), 'x') + ',' + body
        split = len(stream) // 2
        html = ''.join('<script>self.__next_f.push(' + json.dumps([1, part], ensure_ascii=False) + ')</script>'
                       for part in [stream[:split], stream[split:]])
        description, specs = read_product_content(html, {'name': 'Pad'})
        self.assertIn('Vải chống nước.\n\nĐế cao su.', description)
        self.assertEqual({'Kích thước': '900 x 400 x 5 mm', 'Bo viền': 'Có'}, specs)

    def test_ignores_related_product_specs(self):
        html = '<script>self.__next_f.push(' + json.dumps([1, json.dumps({'product': {
            'name': 'Different product', 'specs_table': [{'rows': [{'label': 'DPI', 'value': '999'}]}]}})]) + ')</script>'
        _, specs = read_product_content(html, {'name': 'Pad', 'description': 'Pad description'})
        self.assertEqual({}, specs)

    def test_numeric_flight_reference_does_not_match_timestamp_minutes(self):
        body = '<p>USB condenser 24 bit.</p>'
        stream = '0:{"published":"2026-10-15T00:37:06Z"}\n37:T' + format(len(body.encode()), 'x') + ',' + body + '\n1:{"descriptionBody":"$37"}'
        html = '<script>self.__next_f.push(' + json.dumps([1, stream]) + ')</script>'
        description, specs = read_product_content(html, {'name': 'Micro'})
        self.assertEqual('USB condenser 24 bit.', description)
        self.assertIn('24 bit', specs['Đặc điểm theo nguồn'])

    def test_reads_product_rte_tables_and_excludes_scripts_and_store_ads(self):
        html = '<div class="rte"><p>Nhựa PBT.</p><script>alert(1)</script><table><tr><td>Profile</td><td>Cherry</td></tr></table><p>Mua ngay tại KICAP</p></div><footer>Unrelated text</footer>'
        description, specs = read_product_content(html, {'name': 'Keycap'})
        self.assertEqual('Cherry', specs['Profile'])
        self.assertNotIn('alert', description)
        self.assertNotIn('KICAP', description)
        self.assertNotIn('Unrelated', description)

    def test_preserves_source_bullet_facts_without_inventing_specifications(self):
        description, specs = read_product_content('<div class="rte"><p>- Linear 5 Pin</p><p>- Travel 3.9mm</p></div>', {'name': 'Switch'})
        self.assertEqual('Linear 5 Pin; Travel 3.9mm', specs['Đặc điểm theo nguồn'])
        self.assertNotIn('Lực nhấn', specs)
        self.assertIn('3.9mm', description)

    def test_missing_content_uses_plain_jsonld_description_and_no_fake_specs(self):
        description, specs = read_product_content('<script>self.__next_f.push(broken)</script>', {'description': '<b>Sample</b> keyboard'})
        self.assertEqual('Sample keyboard', description)
        self.assertEqual({}, specs)


if __name__ == '__main__':
    unittest.main()
