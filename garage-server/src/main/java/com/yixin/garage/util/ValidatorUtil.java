package com.yixin.garage.util;

import com.yixin.common.utils.InvokeResult;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.groups.Default;

import org.apache.commons.lang3.StringUtils;

import java.util.Set;

/**
 * 
 * @ClassName: ValidatorUtil
 * @Description 对dto里面添加了条件判断的注解进行校验 
 * @author  YixinCapital -- yangfei02	   
 * @date  2018年5月23日 上午10:42:05
 *
 */
public class ValidatorUtil {
    private static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    public static <T> InvokeResult<T> validate(T param){
        InvokeResult<T> invokeResult = new InvokeResult<>();
        int index = 0;
       Set<ConstraintViolation<T>> set = validator.validate(param, Default.class);
        if(set != null && !set.isEmpty()){
//            String propertyName = null;
            String errorMessage = null;
            StringBuilder sb = new StringBuilder();
            for(ConstraintViolation<T> cv : set){
//                propertyName = cv.getPropertyPath().toString();
                errorMessage = cv.getMessage();
                if(index == set.size()-1){
                    sb.append(errorMessage).append(".");
                }else {
                    sb.append(errorMessage).append(";");
                }
                index ++ ;
            }
            invokeResult.failure(sb.toString());
        } else {
        	invokeResult.success();
        }
        return invokeResult;
    }
    
    // 判断是否为空。
    public static boolean isEmpty(Object value) {
    	return value == null || (value instanceof String && StringUtils.isBlank((CharSequence) value));
    	
//        if (Value == null || "".equals(Value))
//            return true;
//        else
//            return false;
    }
}
