package de.markusressel.mkdocseditor.feature.browser.ui.compose.listentry

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.markusressel.mkdocseditor.R
import java.text.DateFormat
import java.util.Date

@Composable
fun LastModifiedDate(
    modtime: Date,
    modifier: Modifier = Modifier
) {
    Text(
        modifier = modifier,
        text = stringResource(R.string.document_list_entry_last_modified, modtime.formatAsDate()),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
    )
}

private fun Date.formatAsDate(): String {
    return DateFormat.getDateTimeInstance().format(this)
}
