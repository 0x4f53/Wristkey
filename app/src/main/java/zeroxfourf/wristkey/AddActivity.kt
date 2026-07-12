package zeroxfourf.wristkey

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.material3.*
import wristkey.R

class AddActivity : AppCompatActivity() {

    private lateinit var utilities: Utilities

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        utilities = Utilities(applicationContext)

        setContent {
            WristkeyM3Theme {
                AppScaffold(
                    timeText = { TimeText() }
                ) {
                    AddScreen(
                        utilities = utilities,
                        onManualEntry = {
                            startActivity(Intent(this@AddActivity, ManualEntryActivity::class.java))
                            finish()
                        },
                        onWifiTransfer = {
                            if (utilities.wiFiExists(applicationContext)) {
                                startActivity(Intent(this@AddActivity, WiFiTransferActivity::class.java))
                                finish()
                            } else {
                                showNetworkError()
                            }
                        },
                        onScanQRCode = {
                            checkPermission(Manifest.permission.CAMERA, utilities.CAMERA_REQUEST_CODE)
                        },
                        onFileImport = {
                            startActivity(Intent(this@AddActivity, FileImportActivity::class.java))
                            finish()
                        },
                        onAdbImport = {
                            startActivity(Intent(this@AddActivity, AdbImportActivity::class.java))
                            finish()
                        },
                        onBack = { finish() }
                    )
                }
            }
        }
    }

    private fun showNetworkError() {
        CustomFullscreenDialogFragment(
            title = getString(R.string.network_error),
            message = getString(R.string.wifi_error),
            positiveButtonText = null,
            positiveButtonIcon = null,
            negativeButtonText = getString(R.string.back),
            negativeButtonIcon = R.drawable.ic_prev,
        ).show(supportFragmentManager, "CustomFullscreenDialog")
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        else {
            if (requestCode == utilities.CAMERA_REQUEST_CODE) startScannerUI()
        }
    }

    private fun startScannerUI() {
        val intent = Intent(this, QRScannerActivity::class.java)
        startActivityForResult(intent, utilities.CAMERA_REQUEST_CODE + 1)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == utilities.CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) startScannerUI()
            else {
                Toast.makeText(this, getString(R.string.camera_permission_denied), Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == utilities.CAMERA_REQUEST_CODE + 1 && resultCode == RESULT_OK) {
            data?.getStringExtra(utilities.QR_CODE_SCAN_REQUEST)?.let { scannedData ->
                if (scannedData.contains("otpauth://")) {
                    utilities.overwriteLogin(scannedData)
                    startActivity(Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    return
                }
            }
            Toast.makeText(this, getString(R.string.invalid_qr_code), Toast.LENGTH_LONG).show()
        }
    }
}

@Composable
fun AddScreen(
    utilities: Utilities,
    onManualEntry: () -> Unit,
    onWifiTransfer: () -> Unit,
    onScanQRCode: () -> Unit,
    onFileImport: () -> Unit,
    onAdbImport: () -> Unit,
    onBack: () -> Unit
) {
    val listState = rememberScalingLazyListState()

    val amoled = LocalAmoledEnabled.current

    ScreenScaffold(
        scrollState = listState,
        edgeButton = {
            EdgeButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(80.dp),
                border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                    contentColor = if (amoled) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text(
                    text = stringResource(R.string.back),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    ) {
        ScalingLazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                top = 30.dp,
                start = 10.dp,
                end = 10.dp,
                bottom = 84.dp
            ),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            autoCentering = null
        ) {
            item {
                Text(
                    text = stringResource(R.string.add_data),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                Button(
                    onClick = onManualEntry,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                    shape = CircleShape,
                    border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (amoled) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(painterResource(R.drawable.ic_baseline_edit_24), contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(stringResource(R.string.manual_entry), maxLines = 2, softWrap = true)
                    }
                }
            }

            item {
                Button(
                    onClick = onWifiTransfer,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                    shape = CircleShape,
                    border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (amoled) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(painterResource(R.drawable.outline_wifi_24), contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(stringResource(R.string.wifi_transfer), maxLines = 2, softWrap = true)
                    }
                }
            }

            if (utilities.hasCamera()) {
                item {
                    Button(
                        onClick = onScanQRCode,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                        shape = CircleShape,
                        border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                            contentColor = if (amoled) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(painterResource(R.drawable.ic_baseline_search_24), contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(stringResource(R.string.scan_qr_code), maxLines = 2, softWrap = true)
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = onFileImport,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                    shape = CircleShape,
                    border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (amoled) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(painterResource(R.drawable.ic_baseline_add_24), contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(stringResource(R.string.file_import), maxLines = 2, softWrap = true)
                    }
                }
            }

            item {
                Button(
                    onClick = onAdbImport,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                    shape = CircleShape,
                    border = if (amoled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (amoled) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (amoled) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(painterResource(R.drawable.ic_baseline_add_24), contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(stringResource(R.string.adb_transfer_label), maxLines = 2, softWrap = true)
                    }
                }
            }
        }
    }
}
