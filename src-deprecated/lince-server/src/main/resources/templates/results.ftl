<#import "/spring.ftl" as spring/><#-- <@spring.message "front.home.label.legal.1.title"/>-->
<#import "macro/helper.ftl" as helper>
<@layout>
    <@helper.breadcrumb currentPage="Obtener resultados"/>
<div class="row" data-auto-height="true">
    <div class="col-lg-12 col-md-12 col-sm-12  col-xs-12">
        <@helper.portlet captionTitle="Resultados de tu estudio" icon="icon-control-play">
            <div class="row" data-auto-height="true">
                <div id="pie_chart" class="col-lg-4 col-md-4 col-sm-4 col-xs-4" style="height:400px;">
                </div>
                <div id="time_chart" class="col-lg-8 col-md-8 col-sm-8 col-xs-8" style="height:400px;">
                </div>
            </div>
        </@helper.portlet>
    </div>
</div>
</@layout>
<script src="bower_components/highcharts/highcharts.js" type="text/javascript"></script>
<script src="bower_components/highcharts/modules/exporting.js" type="text/javascript"></script>
<script src="bower_components/highcharts/modules/export-data.js" type="text/javascript"></script>
<script src="bower_components/highcharts/modules/data.js" type="text/javascript"></script>
<script src="bower_components/highcharts/modules/drilldown.js" type="text/javascript"></script>
<script type="text/javascript">
    jQuery(document).ready(function () {
        var yAxisTimeDataLabels = {};
        var xAxisTimeLabels = [];
        var timeSeries = [];
        //INFO @ https://www.highcharts.com/docs/chart-concepts/axes
        $.linceApp.global.doAjax('/register/timeStats/', 'GET', null, function (result) {
            console.log("#Timestats start");
            console.log(result);
            console.log("#Timestats end");
            yAxisTimeDataLabels = [];
            if (result.ySeriesLabels) {
                result.ySeriesLabels.forEach(function f(item, index) {
                    yAxisTimeDataLabels[item.key] = item.value;
                })
            }
            if (result.xSeriesLabels) {
                result.xSeriesLabels.forEach(function f(item, index) {
                    xAxisTimeLabels.push(item);
                });
            }
            if (result.series) {
                result.series.forEach(function f(item, index) {
                    console.log(item);
                    var elem = item.y;
//                    timeSeries.push(elem);

                    timeSeries.push(item);
                });
            }
            // TOP LINE CHART
            Highcharts.chart('time_chart', {
                chart: {
                    style: {
                        fontFamily: 'Open Sans'
                    }
                },
                title: {
                    text: result.title,
                    x: -20 //center
                },
                subtitle: {
                    text: result.subtitle,
                    x: -20
                },
                xAxis: {
                    categories: xAxisTimeLabels
                },
                yAxis: {
                    title: {
                        text: 'Categorías'
                    },
                    labels: {
                        formatter: function () {
                            var value = yAxisTimeDataLabels[this.value];
                            return value !== 'undefined' ? value : this.value;
                        }
                    },
                    plotLines: [{
                        value: 0,
                        width: 1,
                        color: '#808080'
                    }],
                    tickInterval: 1
                },
                tooltip: {
                    valueSuffix: 'Cat'
                },
                legend: {
                    layout: 'vertical',
                    align: 'right',
                    verticalAlign: 'middle',
                    borderWidth: 0
                },
                series: timeSeries
            });
        }, function (e) {
            console.log("error al cargar timeStats");
        });
        //INFO https://www.highcharts.com/demo/pie-legend
        $.linceApp.global.doAjax('/register/pieStats/', 'GET', null, function (result) {
            console.log("#pieStats start");
            console.log(result);
            console.log("#pieStats end");
            var pieSerie = [ //result.series data->dataBean, name-ok
                {
                    "name": "Browsers",
                    "colorByPoint": true,
                    "data": [
                        {
                            "name": "Criteria 1",
                            "y": 62.74,
                            "drilldown": "Criteria 1"
                        },
                        {
                            "name": "Criteria 2",
                            "y": 10.57,
                            "drilldown": "Criteria 2"
                        },
                        {
                            "name": "Criteria 3",
                            "y": 7.23,
                            "drilldown": "Criteria 3"
                        },
                        {
                            "name": "Criteria 4",
                            "y": 5.58,
                            "drilldown": "Criteria 4"
                        },
                        {
                            "name": "Criteria 5",
                            "y": 4.02,
                            "drilldown": "Criteria 5"
                        },
                        {
                            "name": "Criteria 6",
                            "y": 1.92,
                            "drilldown": "Criteria 6"
                        },
                        {
                            "name": "Other criteria",
                            "y": 7.62,
                            "drilldown": null
                        }
                    ]
                }
            ];
            //drildown hay que construir un array con dataBean haciendo que [x,y]=[name,y]
            var pieDrilldown = {
                "series": [
                    {
                        "name": "Chrome",
                        "id": "Chrome",
                        "data": [
                            [
                                "v65.0",
                                0.1
                            ],
                            [
                                "v64.0",
                                1.3
                            ],
                            [
                                "v63.0",
                                53.02
                            ],
                            [
                                "v62.0",
                                1.4
                            ],
                            [
                                "v61.0",
                                0.88
                            ],
                            [
                                "v60.0",
                                0.56
                            ],
                            [
                                "v59.0",
                                0.45
                            ],
                            [
                                "v58.0",
                                0.49
                            ],
                            [
                                "v57.0",
                                0.32
                            ],
                            [
                                "v56.0",
                                0.29
                            ],
                            [
                                "v55.0",
                                0.79
                            ],
                            [
                                "v54.0",
                                0.18
                            ],
                            [
                                "v51.0",
                                0.13
                            ],
                            [
                                "v49.0",
                                2.16
                            ],
                            [
                                "v48.0",
                                0.13
                            ],
                            [
                                "v47.0",
                                0.11
                            ],
                            [
                                "v43.0",
                                0.17
                            ],
                            [
                                "v29.0",
                                0.26
                            ]
                        ]
                    },
                    {
                        "name": "Firefox",
                        "id": "Firefox",
                        "data": [
                            [
                                "v58.0",
                                1.02
                            ],
                            [
                                "v57.0",
                                7.36
                            ],
                            [
                                "v56.0",
                                0.35
                            ],
                            [
                                "v55.0",
                                0.11
                            ],
                            [
                                "v54.0",
                                0.1
                            ],
                            [
                                "v52.0",
                                0.95
                            ],
                            [
                                "v51.0",
                                0.15
                            ],
                            [
                                "v50.0",
                                0.1
                            ],
                            [
                                "v48.0",
                                0.31
                            ],
                            [
                                "v47.0",
                                0.12
                            ]
                        ]
                    },
                    {
                        "name": "Internet Explorer",
                        "id": "Internet Explorer",
                        "data": [
                            [
                                "v11.0",
                                6.2
                            ],
                            [
                                "v10.0",
                                0.29
                            ],
                            [
                                "v9.0",
                                0.27
                            ],
                            [
                                "v8.0",
                                0.47
                            ]
                        ]
                    },
                    {
                        "name": "Safari",
                        "id": "Safari",
                        "data": [
                            [
                                "v11.0",
                                3.39
                            ],
                            [
                                "v10.1",
                                0.96
                            ],
                            [
                                "v10.0",
                                0.36
                            ],
                            [
                                "v9.1",
                                0.54
                            ],
                            [
                                "v9.0",
                                0.13
                            ],
                            [
                                "v5.1",
                                0.2
                            ]
                        ]
                    },
                    {
                        "name": "Edge",
                        "id": "Edge",
                        "data": [
                            [
                                "v16",
                                2.6
                            ],
                            [
                                "v15",
                                0.92
                            ],
                            [
                                "v14",
                                0.4
                            ],
                            [
                                "v13",
                                0.1
                            ]
                        ]
                    },
                    {
                        "name": "Opera",
                        "id": "Opera",
                        "data": [
                            [
                                "v50.0",
                                0.96
                            ],
                            [
                                "v49.0",
                                0.82
                            ],
                            [
                                "v12.1",
                                0.14
                            ]
                        ]
                    }
                ]
            };

            Highcharts.chart('pie_chart', {
                chart: {type: 'pie'},
                title: {text: result.title},
                subtitle: {text: result.subtitle},
                plotOptions: {
                    series: {
                        dataLabels: {
                            enabled: true,
                            format: '{point.name}: {point.y:.1f}%'
                        }
                    }
                },
                tooltip: {
                    headerFormat: '<span style="font-size:11px">{series.name}</span><br>',
                    pointFormat: '<span style="color:{point.color}">{point.name}</span>: <b>{point.y:.2f}%</b> of total<br/>'
                },
                "series": pieSerie,
                "drilldown": pieDrilldown
            });

        }, function (e) {
            console.log("error al cargar pieStats");
        });

    });

</script>
