import java.io.File

val f1 = new File("/opt/itc/userCatalog/data")

f1.canWrite
f1.isDirectory

val f2 = new File(f1, "AA.dat")

f2.getAbsolutePath.replace(".dat", ".bac")

"ab".endsWith(".dat")

"a.data".endsWith(".dat")

"a.dat".endsWith(".dat")

