package com.st.demo.use_case


import com.st.demo.api_interface.SensorDataService
import com.st.demo.model.LoginResponse
import com.st.demo.model.LoginUser
import com.st.demo.model.RegistrationResponse
import com.st.demo.model.Sensor
import com.st.demo.model.SensorData
import com.st.demo.model.SensorDataResponse
import com.st.demo.model.User
import com.st.demo.repository_impl.AuthRepositoryImpl
import com.st.demo.repository_impl.SensorRepositoryImpl
import com.st.demo.wrappers.Resource

import javax.inject.Inject


//AUTH -> LoginViewModel
class AuthUsesCases @Inject constructor(private val authRepository: AuthRepositoryImpl) {
    suspend operator fun invoke(loginUser: LoginUser): Resource<LoginResponse> = authRepository.login(loginUser)
}

class SignUpUseCase @Inject constructor(private val authRepository: AuthRepositoryImpl){
    suspend operator fun invoke(user: User): Resource<RegistrationResponse> = authRepository.signup(user)
}

class GetUserByEmail @Inject constructor(private val authRepository: AuthRepositoryImpl){
    suspend operator fun invoke(token: String,email: String): Resource<User> = authRepository.getUserByEmail(token,email)
}

//ML -> RecognitionViewModel
class AddSensor @Inject constructor(private val sensorRepository: SensorRepositoryImpl){
    suspend operator fun invoke(sensor: Sensor): Resource<Sensor> = sensorRepository.create_sensor(sensor)
}

class RemoveSensor @Inject constructor(private val sensorRepository: SensorRepositoryImpl){
    suspend operator fun invoke(sensorId : String): Resource<String> = sensorRepository.remove_sensor(sensorId)
}

class Classifica @Inject constructor(private val sensorRepository: SensorRepositoryImpl){
    suspend operator fun invoke(sensorData: SensorData): Resource<SensorDataResponse> = sensorRepository.classifica(sensorData)
}