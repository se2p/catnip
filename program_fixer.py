#! /usr/bin/env python3

# The scratch ui sometimes does not handle the hint annotation comments as
# intended, especially when cloning blocks. This results in a state where the
# comment is part of some metadata, but the actual block is no longer there,
# which in turn results in an unreadable file.
#
# This script deletes all comments from a sb3-file.

import json
import os
import shutil
import sys
import tempfile
import uuid
from zipfile import ZipFile


def fix_project_file(content):
    content = json.loads(content)

    for t in content["targets"]:
        t["comments"] = {}
        for b in t["blocks"]:
            if "comment" in t["blocks"][b]:
                del t["blocks"][b]["comment"]

    return json.dumps(content)


if __name__ == "__main__":
    filename = os.path.join(".", sys.argv[1])
    temporary = os.path.join(str(tempfile.gettempdir()), str(uuid.uuid4()))

    with ZipFile(filename) as inzip, ZipFile(temporary, "w") as outzip:
        for inzipinfo in inzip.infolist():
            with inzip.open(inzipinfo) as infile:
                if inzipinfo.filename == "project.json":
                    content = infile.read()
                    updated = fix_project_file(content)
                    outzip.writestr(inzipinfo.filename, updated)
                else:
                    outzip.writestr(inzipinfo.filename, infile.read())

    shutil.move(temporary, filename)
