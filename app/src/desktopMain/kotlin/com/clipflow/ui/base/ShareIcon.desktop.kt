package com.clipflow.ui.base

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import com.clipflow.app.generated.resources.Res
import com.clipflow.app.generated.resources.facebook
import com.clipflow.app.generated.resources.linkedin
import com.clipflow.app.generated.resources.mail
import com.clipflow.app.generated.resources.reddit
import com.clipflow.app.generated.resources.telegram
import com.clipflow.app.generated.resources.weibo
import com.clipflow.app.generated.resources.x
import org.jetbrains.compose.resources.painterResource

@Composable
actual fun facebook(): Painter = painterResource(Res.drawable.facebook)

@Composable
actual fun linkedin(): Painter = painterResource(Res.drawable.linkedin)

@Composable
actual fun mail(): Painter = painterResource(Res.drawable.mail)

@Composable
actual fun reddit(): Painter = painterResource(Res.drawable.reddit)

@Composable
actual fun telegram(): Painter = painterResource(Res.drawable.telegram)

@Composable
actual fun weibo(): Painter = painterResource(Res.drawable.weibo)

@Composable
actual fun x(): Painter = painterResource(Res.drawable.x)
