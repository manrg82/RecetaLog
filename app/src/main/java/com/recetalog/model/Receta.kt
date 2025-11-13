package com.recetalog.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Blob

@Entity(tableName = "Receta")
data class Receta (
  @PrimaryKey(autoGenerate = true)@ColumnInfo(name="idreceta") val idreceta:Int,
  @ColumnInfo(name="nmreceta") val nmreceta:String,
  @ColumnInfo (name = "isVerdura")val isVerdura:Boolean,
  @ColumnInfo (name = "isCarne")val isCarne:Boolean,
  @ColumnInfo (name = "isPescado")val isPescado:Boolean,
  @ColumnInfo (name = "isPostre")val isPostre:Boolean,
  @ColumnInfo (name = "isLactosa")val isLactosa:Boolean,
  @ColumnInfo (name = "isFruta")val isFruta:Boolean,
  @ColumnInfo (name="ingredientes")val ingredientes:String,
  @ColumnInfo (name="pasos")val pasos:String,
  @ColumnInfo(name="imagen", typeAffinity = ColumnInfo.BLOB) val imagen: ByteArray? = null
  )
