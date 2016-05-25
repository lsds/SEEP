import os,fnmatch,shutil

def chmod_dir(path, perms=0777):
    os.chmod(path, perms)
    for root,dirs,files in os.walk(path):
        for d in dirs:
            os.chmod(os.path.join(root,d), perms)
        for f in files:
            os.chmod(os.path.join(root,f), perms)

def copy_pdfs(srcdir, destdir):
    for file in os.listdir(srcdir):
        if fnmatch.fnmatch(file, '*.pdf'):
            shutil.copy("%s/%s"%(srcdir,file), destdir)

def copy_results(srcdir, destdir):
    for file in os.listdir(srcdir):
        if fnmatch.fnmatch(file, 'all-k-*.data'):
            shutil.copy("%s/%s"%(srcdir,file), destdir)

def pybool_to_javastr(mybool):
    return "true" if mybool else "false"

