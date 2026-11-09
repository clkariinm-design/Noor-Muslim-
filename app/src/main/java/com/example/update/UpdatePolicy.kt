package com.example.update

object UpdatePolicy {
    fun requiresUpdate(currentVersionCode: Int, latestVersionCode: Int, mandatory: Boolean): Boolean {
        return mandatory && latestVersionCode > currentVersionCode
    }
}
