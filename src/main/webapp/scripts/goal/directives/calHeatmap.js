'use strict';

angular.module('milestogo')
    .directive('calHeatmap', function () {
        function link(scope, el) {
            var config = scope.config;
            var element = el[0];
            var cal = new CalHeatMap();
            cal.init({
                    itemSelector: element,
                    domain: config.domain ? config.domain : "month",
                    subDomain: config.subDomain ? config.subDomain : "x_day",
                    subDomainTextFormat: config.subDomainTextFormat ? config.subDomainTextFormat : "%d",
                    data: config.data ? config.data : "",
                    start: config.start ? config.start : new Date(),
                    minDate: config.minDate ? config.minDate : new Date(),
                    maxDate: config.maxDate ? config.maxDate : new Date(),
                    cellSize: config.cellSize ? config.cellSize : 32,
                    range: config.range ? config.range : 1,
                    domainGutter: config.domainGutter ? config.domainGutter : 25,
                    legend: config.legend ? config.legend : [5, 10, 15, 20],
                    itemName: config.itemName ? config.itemName : "item",
                    previousSelector: "#previous",
                    displayLegend: false,
                    domainLabelFormat: config.domainLabelFormat ? config.domainLabelFormat : "%B-%Y",
                    nextSelector: "#next",
                    onMinDomainReached: function (hit) {
                        if (hit) {
                            $("#previous").attr("disabled", "disabled");
                        } else {
                            $("#previous").attr("disabled", false);
                        }
                    },
                    onMaxDomainReached: function (hit) {
                        if (hit) {
                            $("#next").attr("disabled", "disabled");
                        } else {
                            $("#next").attr("disabled", false);
                        }
                    }
                }

            )
            ;
        }

        return {
            template: '<div id="cal-heatmap" align="center" config="config"></div>',
            restrict: 'E',
            link: link,
            scope: { config: '=' }
        };
    })
;
