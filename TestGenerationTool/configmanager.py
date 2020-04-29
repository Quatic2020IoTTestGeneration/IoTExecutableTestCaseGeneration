import json


def build_java_header(imports, clsname, vars):
    res = ''
    res += imports
    res += '\npublic class '+clsname+' {\n'

    for v in vars:
        res += vars[v]['options']['visibility']
        if('modifier' in vars[v]):
            res += ' '+vars[v]['modifier']
        res += ' '+vars[v]['type']
        res += ' '+v
        if('value' in vars[v]):
            res += ' = '+str(vars[v]['value'])
        res += ';\n'
    res += '\n'
    return res


def get_python_config():
    with open('config/python_config.json', 'r') as f:
        test_lang_cfg = json.load(f)
    header = test_lang_cfg['header']
    with open('config/model_lang_map_py.json', 'r') as f:
        model_lang_map = json.load(f)
    return header, model_lang_map, test_lang_cfg


def get_java_config(fnam):
    with open('config/java_config.json', 'r') as f:
        test_lang_cfg = json.load(f)
    with open('config/variables.json', 'r') as f:
        java_vars = json.load(f)
    with open('config/model_lang_map.json', 'r') as f:
        model_lang_map = json.load(f)
    header = build_java_header(test_lang_cfg['imports'], fnam[:-5], java_vars)
    return header, model_lang_map, test_lang_cfg