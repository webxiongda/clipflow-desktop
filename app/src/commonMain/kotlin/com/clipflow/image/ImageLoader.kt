package com.clipflow.image

import com.clipflow.utils.Loader
import okio.Path

interface FaviconLoader : Loader<String, Path>

interface FileExtImageLoader : Loader<Path, Path>
