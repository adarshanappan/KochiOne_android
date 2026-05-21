import re
import sys

file_path = "/Users/subinkurian/KochiOne_android_3/app/src/main/java/com/kochione/kochi_one/MainActivity.kt"

with open(file_path, "r") as f:
    content = f.read()

# find all imports
imports = re.findall(r"^import ([\w\.]+)", content, re.MULTILINE)
unused_imports = []

content_without_imports = re.sub(r"^import .*\n", "", content, flags=re.MULTILINE)

for imp in imports:
    # get the simple name (last part)
    simple_name = imp.split(".")[-1]
    
    # check if simple_name is used in content_without_imports
    # we need a word boundary so we don't match substrings
    pattern = r"\b" + re.escape(simple_name) + r"\b"
    if not re.search(pattern, content_without_imports):
        unused_imports.append(imp)

# remove unused imports from the file
new_content = content
for imp in unused_imports:
    new_content = re.sub(r"^import " + re.escape(imp) + r"\n", "", new_content, flags=re.MULTILINE)

with open(file_path, "w") as f:
    f.write(new_content)

print(f"Removed {len(unused_imports)} unused imports.")
