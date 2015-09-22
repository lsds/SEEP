import os

def chmod_dir(path, perms=0777):
    os.chmod(path, perms)
    for root,dirs,files in os.walk(path):
        for d in dirs:
            os.chmod(os.path.join(root,d), perms)
        for f in files:
            os.chmod(os.path.join(root,f), perms)

def pybool_to_javastr(mybool):
    return "true" if mybool else "false"

