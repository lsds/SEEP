from core.api import coreapi
from core.emane.emane import EmaneGlobalModel

def raw_emane_global_conf_msg(wlanid):
	"""
	tlvdata = ""
	tlvdata += coreapi.CoreConfTlv.pack(coreapi.CORE_TLV_CONF_OBJ, "emane")
        tlvdata += coreapi.CoreConfTlv.pack(coreapi.CORE_TLV_CONF_NODE, wlanid)
        tlvdata += coreapi.CoreConfTlv.pack(coreapi.CORE_TLV_CONF_DATA_TYPES,
                                            (coreapi.CONF_DATA_TYPE_STRING,))
        #datatypes = tuple( map(lambda v: coreapi.CONF_DATA_TYPE_STRING,
        #                       EmaneGlobalModel.getnames()) )
	defaults = EmaneGlobalModel.getdefaultvalues()
	names = EmaneGlobalModel.getnames()
	vals = [ location.refxyz[0], location.refxyz[1], location.refgeo[0], location.refgeo[1], location.refgeo[2], location.refscale ]
	vals = "|".join(map(str, vals))
        tlvdata += coreapi.CoreConfTlv.pack(coreapi.CORE_TLV_CONF_VALUES, vals)
        msg = coreapi.CoreConfMessage.pack(0, tlvdata)
	return msg
	"""
	global_names = EmaneGlobalModel.getnames()
	global_values = list(EmaneGlobalModel.getdefaultvalues())
	global_values[ global_names.index('otamanagerdevice') ] = 'ctrl1'
	global_values[ global_names.index('otamanagergroup') ] = '224.1.2.9:45702'
	msg = EmaneGlobalModel.toconfmsg(flags=0, nodenum=wlanid,
				     typeflags=coreapi.CONF_TYPE_FLAGS_NONE, values=global_values)
	print 'Created emane conf msg=%s'%str(msg)
	return msg

def to_msg(raw_msg):
        hdr = raw_msg[:coreapi.CoreMessage.hdrsiz]
        msgtype, flags, msglen = coreapi.CoreMessage.unpackhdr(hdr)
        msgcls = coreapi.msg_class(msgtype)
        return msgcls(flags, hdr, raw_msg[coreapi.CoreMessage.hdrsiz:])

def location_conf_msg(location):
	tlvdata = ""
	tlvdata += coreapi.CoreConfTlv.pack(coreapi.CORE_TLV_CONF_OBJ, "location")
        tlvdata += coreapi.CoreConfTlv.pack(coreapi.CORE_TLV_CONF_TYPE, coreapi.CONF_TYPE_FLAGS_NONE)
        tlvdata += coreapi.CoreConfTlv.pack(coreapi.CORE_TLV_CONF_DATA_TYPES,
                                            (coreapi.CONF_DATA_TYPE_STRING,))
	vals = [ location.refxyz[0], location.refxyz[1], location.refgeo[0], location.refgeo[1], location.refgeo[2], location.refscale ]
	vals = "|".join(map(str, vals))
        tlvdata += coreapi.CoreConfTlv.pack(coreapi.CORE_TLV_CONF_VALUES, vals)
        msg = coreapi.CoreConfMessage.pack(0, tlvdata)
	return msg
