package jp.pinolab.hitokoma.feature.selector.domain

import kotlinx.datetime.LocalDate

/**
 * すでに指定日に写真が存在する場合にスローされるドメイン例外
 */
class PhotoAlreadyExistsException(val date: LocalDate) :
    Exception("A photo for date $date already exists. Only 1 photo per day is allowed.")