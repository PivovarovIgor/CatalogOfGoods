package ru.brauer.catalogofgoods.data.glidemodel

import org.apache.commons.net.ftp.FTPClient

data class FtpModel(
    val ftpClient: FTPClient,
    val fileName: String
)
