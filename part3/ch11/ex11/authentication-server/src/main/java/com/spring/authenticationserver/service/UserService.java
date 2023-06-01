package com.spring.authenticationserver.service;

import com.spring.authenticationserver.entity.Otp;
import com.spring.authenticationserver.entity.User;
import com.spring.authenticationserver.repository.OtpRepository;
import com.spring.authenticationserver.repository.UserRepository;
import com.spring.authenticationserver.util.GenerateCodeUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@AllArgsConstructor
@Service
@Transactional
public class UserService {

    private PasswordEncoder passwordEncoder;

    private UserRepository userRepository;

    private OtpRepository otpRepository;

    /**
     * 신규 유저를 등록하는 메서드
     */
    public void addUser(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword())); // 인코딩 DB에 값을 저장
        userRepository.save(user);
    }

    /**
     * 1 차 인증
     *
     * 유저 인증 정보를 확인하고 Otp 를 발급하는 메서드
     */
    public void auth(User user){
        Optional<User> o = userRepository.findByUsername(user.getUsername()); // 데이터베이스에서 사용자 검색

        if(o.isPresent()){
            User u = o.orElse(null);
            if(passwordEncoder.matches(user.getPassword(), u.getPassword())){ // 비밀번호 일치 확인
                /**
                 * 데이터베이스에 인코딩된 비밀번호와 입력된 비밀번호가 같으면 otp 를 만든다.
                 * otp 가 생성되면 sms 를 통해서 사용자에게 전달된다. (이 부분은 생략)
                 */
                renewOtp(u);
            }else{
                throw new BadCredentialsException("잘못된 비밀번호 입니다.");
            }
        }else{
            throw new BadCredentialsException("등록된 사용자가 없습니다.");
        }
    }

    /**
     * 2 차 인증 (MFA - Multi Factor Authentication 277)
     *
     * OTP 를 발급 받은 유저와 데이터베이스에 저장된 유저 OTP 정보를 확인한다.
     * 발급 받은 OTP 의 정보가 맞는지 체크하는 메서드
     */
    public boolean check(Otp otpToValidate){

        Optional<Otp> userOtp = otpRepository.findByUsername(otpToValidate.getUsername());

        if(userOtp.isPresent()){
            Otp otp = userOtp.get();

            if(otpToValidate.getCode().equals(otp.getCode())){
            return true;
            }
        }
        return false;
    }


    /**
     * OTP 생성 메서드
     */
    private void renewOtp(User u){
        String code = GenerateCodeUtil.generateCode(); // otp 생성

        Optional<Otp> userOtp = otpRepository.findByUsername(u.getUsername()); // 사용자 이름으로 검색한 otp
        if(userOtp.isPresent()){
            Otp otp = userOtp.get();
            otp.setCode(code);
        }else{
            Otp otp = new Otp();
            otp.setUsername(u.getUsername());
            otp.setCode(code);

            otpRepository.save(otp);
        }
    }

}
