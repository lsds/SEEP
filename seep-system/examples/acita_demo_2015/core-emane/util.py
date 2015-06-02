import os

def chmod_dir(path):
    os.chmod(path, 0777)
    for root,dirs,files in os.walk(path):
        for d in dirs:
            os.chmod(os.path.join(root,d), 0777)
        for f in files:
            os.chmod(os.path.join(root,f), 0777)
