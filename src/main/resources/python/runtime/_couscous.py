def _div_round_to_zero(a, b):
    return -(-a // b) if (a < 0) ^ (b < 0) else a // b

def _mod_round_to_zero(a, b):
    return -(-a % b) if (a < 0) ^ (b < 0) else a % b
