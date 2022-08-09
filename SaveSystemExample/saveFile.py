from asyncore import write
from os import system
def SaveInFile(SaveFile):
    if SaveFile==1:
        return write.system("\SaveFiles\save1.sql")
    if SaveFile==2:
        return write.system("\SaveFiles\save2.sql")
    if SaveFile==3:
        return write.system("\SaveFiles\save3.sql")
    if SaveFile==4:
        return write.system("\SaveFiles\save4.sql")
    if SaveFile==5:
        return write.system("\SaveFiles\save5.sql")
    