'use strict';

angular.module('miles2run-profile')
    .service('ConfigService', function ConfigService($location) {
        return {
            getBaseUrl: function () {
                if ($location.port() === 9000) {
                    return "http://localhost:8080/miles2run/api/v1/";
                } else if ($location.port() === 8080) {
                    return "/miles2run/api/v1/";
                } else {
                    return "/api/v1/";
                }
            },

            appContext: function () {
                if ($location.port() === 8080) {
                    return "/miles2run/";
                } else {
                    return "/";
                }
            }

        };
    });
