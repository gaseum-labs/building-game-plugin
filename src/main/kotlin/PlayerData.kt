import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.UUID
import kotlin.collections.HashMap
import kotlin.random.Random

class PlayerData (
	val uuid: UUID,
	val name: String,
	val dummy: Boolean,
	val buildingBlock: Material,
) {
	companion object {
		private var list = HashMap<UUID, PlayerData>()
		/** must be updated in tandem with list */
		private var nameList = HashMap<String, PlayerData>()

		fun getOrCreate(player: Player): PlayerData {
			return list.getOrPut(player.uniqueId) {
				val newData = PlayerData(player.uniqueId, player.name, false, Material.GRASS_BLOCK)
				nameList[player.name] = newData
				newData
			}
		}

		fun getUnsafe(uuid: UUID): PlayerData {
			return list[uuid] ?: throw Exception("Playerdata does not exist?")
		}

		fun getName(name: String): PlayerData? {
			return nameList[name]
		}

		fun createDummy() {
			val uuid = UUID.randomUUID()
			val name = takeFrom(dummyNames, dummyNamesTaken)
			val newData = PlayerData(uuid, name, true, takeFrom(buildingBlocks, buildingBlocksTaken))
			list[uuid] = newData
			nameList[name] = newData
		}

		fun all(): Set<MutableMap.MutableEntry<UUID, PlayerData>> {
			return list.entries
		}

		fun clearDummies() {
			list = list.filter { (_, data) -> if (data.dummy) {
				nameList.remove(data.name)
				putBack(dummyNames, dummyNamesTaken, data.name)
				putBack(buildingBlocks, buildingBlocksTaken, data.buildingBlock)
				false
			} else true } as HashMap<UUID, PlayerData>
		}

		/** clear dummies, reset teleport locations */
		fun cleanData() {
			clearDummies()
			list.forEach { (_, data) -> data.teleportLocation = null }
		}
	}

	var participating: Boolean = false
	var teleportLocation: Location? = null

	fun takeTeleportLocation(): Location? {
		val ret = teleportLocation
		teleportLocation = null
		return ret
	}
}

fun <T>takeFrom(array: ArrayList<T>, taken: Array<Boolean>): T {
	var index = Random.nextInt(array.size)
	while (taken[index]) {
		index = (index + 1) % array.size
	}
	taken[index] = true
	return array[index]
}

fun <T>putBack(array: ArrayList<T>, taken: Array<Boolean>, value: T) {
	taken[array.indexOf(value)] = false
}

val buildingBlocks = Material.values().filter {material ->
	!material.isLegacy &&
	material.isSolid
} as ArrayList
val buildingBlocksTaken = Array(buildingBlocks.size) { false }

val dummyNames = arrayListOf(
"(NULL)"
,"(any)","(created)","1","11111111","12.x","1502","18140815","1nstaller","2","22222222","30","31994","4Dgifts","5","6.x","7","ADAMS","ADLDEMO","ADMIN","ADMINISTRATOR","ADVMAIL","ALLIN1","ALLIN1MAIL","ALLINONE","ANDY","AP","AP2SVP","APL2PP","APPLSYS","APPLSYSPUB","APPS","APPUSER","AQ","AQDEMO","AQJAVA","AQUSER","ARAdmin","ARCHIVIST","AUDIOUSER","AUTOLOG1","Admin"
,"Admin1","Admin5","Administrator","AdvWebadmin","Airaya","Any","Audrey","BACKUP","BATCH","BATCH1","BATCH2","BC4J","BLAKE","BRIO_ADMIN","Best1_User","Bill","Bobo","CATALOG","CCC","CDEMO82","CDEMOCOR","CDEMORID","CDEMOUCB","CENTRA","CHEY_ARCHSVR"
,"CICSUSER","CIDS","CIS","CISINFO","CLARK","CMSBATCH","CMSUSER","COMPANY","COMPIERE","CPNUC","CPRM","CQSCHEMAUSER","CSMIG","CSPUSER","CTXDEMO","CTXSYS","CVIEW","Cisco","ClearOne","Coco","Craft","Crowd","D-Link","DATAMOVE","DBA","DBDCCICS","DBI","DBSNMP","DCL","DDIC","DECMAIL"
,"DECNET","DEFAULT","DEMO","DEMO1","DEMO2","DEMO3","DEMO4","DEMO8","DEMO9","DES","DESQUETOP","DEV2000_DEMOS","DIP","DIRECT","DIRMAINT","DISCOVERER_ADMIN","DISKCNT","DS","DSA","DSGATEWAY","DSL","DSSYS","Demo","Denny","Developer","Draytek","EARLYWATCH","EAdmin<systemid>","EJSADMIN","EMP","EREP"
,"ESSEX","ESTOREUSER","ESubscriber","EVENT","EXFSYS","Ezsetup","FAX","FAXUSER","FAXWORKS","FIELD","FINANCE","FND","FORSE","FROSTY","FSFADMIN","FSFTASK1","FSFTASK2","Flo","GATEWAY","GCS","GL","GPFD","GPLD","GUEST","Guest","HCPARK","HELLO","HELP","HELPDESK","HLW","HOST"
,"HPLASER","HPSupport","HR","IBMUSER","IDMS","IDMSSE","IIPS","IMAGEUSER","IMEDIA","INFO","INGRES","IPC","IPFSERV","ISPVM","IVPM1","IVPM2","JDE","JMUSER","JONES","JWARD","Jetform","Joe","KeyOperator","L2LDEMO","LASER","LASERWRITER","LBACSYS","LDAP_Anonymous","LIBRARIAN","LIBRARY"
,"LINK","LR-ISDN","Liebert","LocalService","Lonnie","MAIL","MAILER","MAINT","MANAGER","MASTER","MBMANAGER","MBWATCH","MCUser","MDDEMO","MDDEMO_CLERK","MDDEMO_MGR","MDSYS","MDaemon","MFG","MGE","MGR","MGWUSER","MICRO","MIGRATE","MILLER","MMO2","MODTEST","MOESERV","MOREAU","MSHOME","MTSSYS","MTS_USER","MXAGENT","Manager","Minnie"
,"Moe","NAMES","NETCON","NETMGR","NETNONPRIV","NETOP","NETPRIV","NETSERVER","NETWORK","NEVIEW","NEWINGRES","NEWS","NSA","NetLinx","Nice-admin","OAS_PUBLIC","OCITEST","ODM","ODM_MTR","ODS","ODSCOMMON","OE","OEMADM","OEMREP","OLAPDBA","OLAPSVR","OLAPSYS","OLTSEP","OMWB_EMULATION","OO","OP1","OPENSPIRIT","OPER","OPERATIONS"
,"OPERATNS","OPERATOR","OPERVAX","ORACACHE","ORAREGSYS","ORASSO","ORDPLUGINS","ORDSYS","OSP22","OUTLN","OWA","OWA_PUBLIC","OWNER","Oper","Operator","OutOfBox","PACSLinkIP","PANAMA","PATROL","PBX","PCUSER","PDMREMI","PDP11","PDP8","PENG","PERFSTAT","PHANTOM","PLEX","PLMIMService","PLSQL","PM","PO","PO7","PO8","PORTAL30","PORTAL30_DEMO","PORTAL30_PUBLIC","PORTAL30_SSO","PORTAL30_SSO_PS","POST"
,"POSTMASTER","POWERCARTUSER","POWERCHUTE","PRIMARY","PRINT","PRINTER","PRIV","PROCAL","PRODBM","PRODCICS","PROG","PROMAIL","PSFMAINT","PUBSUB","PUBSUB1","PVM","Polycom","QDBA","QS","QSRV","QS_ADM","QS_CB","QS_CBADM","QS_CS","QS_ES","QS_OS","QS_WS","RAID","RDM470","RE","REPADMIN","REPORT","REPORTS_USER","REP_MANAGER","REP_OWNER","RJE","RMAIL","RMAN"
,"RMUser1","ROOT","ROUTER","RSBCMON","RSCS","RSCSV2","Replicator","Rodopi","SA","SABRE","SAMPLE","SAP","SAP*","SAPCPIC","SAPR3","SAVSYS","SDOS_ICSAP","SECDEMO","SENTINEL","SERVICECONSUMER1","SETUP","SFCMI","SFCNTRL","SH","SITEMINDER","SLIDE","SMART","SMDR","SPOOLMAN","SQLDBA","SQLUSER","STARTER","STRAT_USER","STUDENT","SUPERVISOR","SWPRO","SWUSER","SYMPA","SYS"
,"SYSA","SYSADM","SYSADMIN","SYSCKP","SYSDBA","SYSDUMP1","SYSERR","SYSMAINT","SYSMAN","SYSTEM","SYSTEST","SYSTEST_CLIG","SYSWRM","Service","SuperUser","Sysop","TAHITI","TDISK","TDOS_ICSAP","TELEDEMO","TEMP","TEST","TESTPILOT","TMAR#HWMT8007079","TMSADM","TOAD","TRACESVR","TRAVEL","TSAFVM","TSDEV","TSUSER","TURBINE","Tasman","Telecom","UETP","ULTIMATE","USER","USER0","USER1"
,"USER2","USER3","USER4","USER5","USER6","USER7","USER8","USER9","USERID","USERP","USER_TEMPLATE","UTLBSTATU","User","Username","VASTEST","VAX","VCSRV","VIDEOUSER","VIF_DEVELOPER","VIRUSER","VM3812","VMARCH","VMASMON","VMASSYS","VMBACKUP","VMBSYSAD","VMMAP","VMS","VMTAPE","VMTLIBR","VMUTIL","VNC","VOL-0215","VRR1","VSEIPO","VSEMAINT","VSEMAN","VTAM","VTAMUSER","WANGTEK","WEBADM","WEBCAL01","WEBDB","WEBREAD","WINDOWS_PASSTHRU"
,"WINSABRE","WKSYS","WP","WWW","WWWUSER","WebAdmin","WinCCAdmin","WinCCConnect","XPRT","XXSESS_MGRYY","Yak","__super","accounting","adfexc","adm","admin","admin2","administrator","adminstrator","admn","alien","anon","anonymous","apc","at4400","author","autocad","backuponly","backuprestore","basisk","bbs","bciim","bcim","bcms","bcnas","bewan","billy-bob","bin","blue","boss","both","bpel","browse","bsxuser","bubba","builtin","cablecom"
,"ccrusr","ceconsl","cgadmin","checkfs","checkfsys","checksys","circ","cisco","clearone","client","cmaker","cn=orcladmin","conferencing","core","craft","ctb_admin","cusadmin","cust","dadmin","daemon","db2fenc1","db2inst1","dbase","debug","default","default.password","demo","demos","deskalt","deskman","desknorm","deskres","dev","device","dhs3mt","dhs3pms","diag","distrib","disttech","dni","dos","dpn","dvstation","eagle","echo","enable","eng"
,"enquiry","epiq_api","factory","fal","fam","fastwire","fax","fg_sysadmin","field","firstsite","ftp","fwadmin","games","glftpd","god1","god2","gonzo","gopher","gropher","guest","guest1","guru","halt","hello","host","hqadmin","hsa","hscroot","http","ibm","iceman","iclock","ilom-admin","ilom-operator","ilon","inads","informix","init","install","installer","internal","itsadmin","jamfsoftware","jasperadmin","jj","joe"
,"joeuser","johnson","kermit","l2","l3","lansweeperuser","lexar","locate","login","lp","lpadm","lpadmin","lynx","mail","mailadmin","maint","maint1","maint2","maintainer","man","manager","mary","master","me","mfd","mlusr","mobile","monitor","mountfs","mountfsys","mountsys","mtch","mtcl","mydlp","n.a","naadmin","ncrm","netbotz","netlink","netman","netopia","netrangr","netscreen","news","nm2user"
,"nms","nobody","none","nsroot","ntpupdate","nuucp","ods","onlime_r","op","openfiler","operator","oracle","overseer","piranha","pnadmin","politically","poll","postgres","postmaster","powerdown","prime","primenet","primeos","primos_cs","prtgadmin","pw","pwrchute","qpgmr","qsecofr","qserv","qsrv","qsrvbas","qsvr","qsysopr","questra","quser","rainer","rapport","rcust","rdc123","read","readonly","recover","redline"
,"replicator","restoreonly","ripeop","rje","ro","role","role1","roo","root","root","root@localhost","rsadmin","rw","rwa","s1stem","sa","sadmin","satan","savelogs","scott","secofr","security","sedacm","service","servlet","setpriv","setup","shutdown","signa","siteadmin","snake","snmp","software","spcl","ssadmin","ssladmin","ssp","status","storwatch","su","super","superdba","superuser","supervisor"
,"support","sweex","sync","sys","sysadm","sysadmin","sysbin","sysopr","system","system_admin","t3admin","tasman","teacher","tech","technician","tele","temp1","test","theman","tomcat","toor","topicalt","topicnorm","topicres","tour","tridium","trmcnfg","trouble","tutor","umountfs","umountfsys","umountsys","unix","user","userNotUsed","user_analyst","user_approver","user_author","user_checker","user_designer","user_editor","user_expert","user_marketer","user_pricer","user_publisher","username","uucp"
,"uucpadm","uwmadmin","vcr","vgnadmin","viewer","viewuser","voadmin","volition","vpasp","web","web_api","webadmin","weblogic","webmaster","whd","wlcsystem","wlpisystem","wlse","wradmin","write","www","xmi_demo","zyfwp"
)
val dummyNamesTaken = Array(dummyNames.size) { false }
