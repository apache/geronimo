/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.sample.RegisterValidation.Constraints;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidDayValidator implements ConstraintValidator<ValidDay, String> {

    @Override
    public void initialize(ValidDay validDay) {
// validDay is the constrait annotation
    }

    @Override
    public boolean isValid(String date, ConstraintValidatorContext context) {
//date is the value to be validated
//arg1 is the validator context,can be used to modify message.
        if(date==null)
            return true;
        Pattern p = Pattern.compile("\\d{4}+[-]\\d{1,2}+[-]\\d{1,2}+");
        Matcher m = p.matcher(date);
        if (!m.matches()) {
            context.buildConstraintViolationWithTemplate("Bad date format,should be like 2012-12-30");
            return false;
        }

        String[] array = date.split("-");
        int year = Integer.valueOf(array[0]);
        int month = Integer.valueOf(array[1]);
        int day = Integer.valueOf(array[2]);

        if (month < 1 || month > 12) {
            context.buildConstraintViolationWithTemplate("The month should between January to December");
            return false;
        }
        int[] monthLengths = new int[]{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        if (isLeapYear(year)) {
            monthLengths[1] = 29;
        } 
        int monthLength = monthLengths[month - 1];
        if (day < 1 || day > monthLength) {
            context.buildConstraintViolationWithTemplate("The day of the month should between 1 and "+monthLength);
            return false;
        }
        return true;
    }

    private boolean isLeapYear(int year) {
        return ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0);
    }
}
