package com.example.zooavito.validator;

import com.example.zooavito.model.User;
import com.example.zooavito.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class UserValidator implements Validator {

    @Autowired
    private UserService userService;

    @Override
    public boolean supports(Class<?> aClass) {
        return User.class.equals(aClass);
    }

    @Override
    public void validate(Object target, Errors errors) {
        User user = (User) target;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "Required");
        if(userService.findByEmail(user.getEmail()) != null){
            errors.rejectValue("email", "DuplicateEmail");
        }

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "Required");
        if(user.getPassword().length() < 6){
            errors.rejectValue("password", "SizePasswordLess");
        }
        if(user.getPassword().length() > 32){
            errors.rejectValue("password", "SizePasswordGreated");
        }

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "confirmPassword", "Required");
        if(!user.getConfirmPassword().equals(user.getPassword())){
            errors.rejectValue("confirmPassword", "PasswordsDontMatch");
        }
    }
}
