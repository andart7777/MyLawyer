// Для предотвращение повторных сообщений от бота при переключении меню
package com.example.mylawyer.utils

open class Event<out T>(private val content: T) {

    private var hasBeenHandled = false

    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) null else {
            hasBeenHandled = true
            content
        }
    }

    fun peekContent(): T = content
}
