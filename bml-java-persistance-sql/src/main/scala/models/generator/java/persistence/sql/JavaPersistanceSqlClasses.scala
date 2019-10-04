package models.generator.java.persistence.sql

object JavaPersistanceSqlClasses
  extends JavaPersistanceSqlCodeGenerator {
  override def getJavaDocFileHeader() = "WARNING: not all features (notably unions) and data types work with the java generator yet. \nplease contact brianmlima@gmail.com"
}
