package com.ti4all.agendaapp

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ViaCepService {
    @GET("{cep}/json/")
    fun getCep(@Path("cep")cep : String): Call<CepResponse>
}