def RPGIDs(id_rp):
  if id_rp==1:
    print("Fantasy")
    return None
  if id_rp==2:
    print("Modern")
    return None
  if id_rp==3:
    print("Military")
    return None
  if id_rp==4:
    print("Futuristic")
    return None
  if id_rp==5:
    print("Medieval")
    return None
  if id_rp>5 or id_rp<1:
    print("ID entré pas valide")
    return False
