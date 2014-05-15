'use strict';

angular.module('milestogo')
    .directive('ignoreField', function () {
        return {
            priority: 500,
            compile: function (el, attrs) {
                attrs.$set('type',
                    null,
                    false
                );
            }
        }
    });