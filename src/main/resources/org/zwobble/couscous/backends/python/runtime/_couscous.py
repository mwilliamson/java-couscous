def _div_round_to_zero(a, b):
    return -(-a // b) if (a < 0) ^ (b < 0) else a // b

def _mod_round_to_zero(a, b):
    return -(-a % b) if (a < 0) ^ (b < 0) else a % b


def signature(*args):
    def wrap(func):
        func._couscous_signature = args
        return func
    
    return wrap


class TypeParameter(object):
    def __init__(self, declaring_class, name):
        self.declaring_class = declaring_class
        self.name = name


def couscous_type(name):
    def wrap(type_):
        for key in dir(type_):
            method = getattr(type_, key)
            signature = getattr(method, "_couscous_signature", None)
            if signature is not None:
                delattr(type_, key)
                typed_key = "__".join(
                    [method.__name__] +
                    [_type_to_string(arg) for arg in signature]
                )
                setattr(type_, typed_key, method)
    
    return name


def _type_to_string(type_):
    if type_ == list:
        return "array"
    else:
        raise Exception("Unhandled type: {0}".format(type_))


Array = list
