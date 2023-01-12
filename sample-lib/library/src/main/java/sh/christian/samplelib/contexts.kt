package sh.christian.samplelib

import android.content.Context
import io.reactivex.rxjava3.core.Single

fun Context.myAppPackage(): Single<String> {
  return Single.fromCallable { packageName }
}
