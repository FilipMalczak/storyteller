#poor mans implementation, but so be it
from typing import Callable


def extract_code(f: Callable) -> str:
    first_line = f.__code__.co_firstlineno
    line_numbers = list({ first_line}.union({ line_no for (start, end, line_no) in f.__code__.co_lines() if line_no is not None }))
    line_numbers.sort()
    lines = []
    with open(f.__code__.co_filename) as source_file:
        for i, l in enumerate(source_file.readlines()):
            if i+1 in line_numbers:
                lines.append(l)
                line_numbers.pop(0)
    return "".join(lines).rstrip()

if __name__ == "__main__":
    def f1(): pass

    def f2(a): ...

    def f3():
        pass

    def f4(abc, defg: str):
        print("xyz")
        print("hello")

    assert extract_code(f1) == "    def f1(): pass"
    assert extract_code(f2) == "    def f2(a): ..."
    assert extract_code(f3) == """    def f3():
        pass"""
    assert extract_code(f4) == """    def f4(abc, defg: str):
        print("xyz")
        print("hello")"""