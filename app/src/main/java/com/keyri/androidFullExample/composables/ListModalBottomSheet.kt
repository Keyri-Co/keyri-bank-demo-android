package com.keyri.androidFullExample.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.keyri.androidFullExample.data.ModalListItem
import com.keyri.androidFullExample.theme.primaryDisabled

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListModalBottomSheet(
    sheetState: SheetState,
    title: String,
    list: List<ModalListItem>,
    onListItemClicked: (ModalListItem) -> Unit,
    onDismissRequest: () -> Unit
) {
    // TODO: Use it to show modal lists

    ModalBottomSheet(
        containerColor = MaterialTheme.colorScheme.onPrimary,
        dragHandle = null,
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Column {
            Text(
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                text = title
            )

            LazyColumn(modifier = Modifier.padding(vertical = 20.dp)) {
                items(list) {
                    Row(
                        modifier = Modifier
                            .wrapContentHeight()
                            .padding(8.dp)
                            .fillMaxWidth()
                            .clickable {
                                onListItemClicked(it)
                            }
                    ) {
                        it.iconRes?.let { iconRes ->
                            Image(
                                modifier = Modifier.size(54.dp),
                                painter = painterResource(iconRes),
                                contentDescription = null
                            )
                        }

                        Text(
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(start = 16.dp),
                            textAlign = TextAlign.Center,
                            text = it.text
                        )
                    }

                    if (list.indexOf(it) != list.lastIndex) {
                        HorizontalDivider(color = primaryDisabled, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}
