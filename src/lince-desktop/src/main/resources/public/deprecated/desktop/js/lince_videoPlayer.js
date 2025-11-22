(function ($) {
        if (!$.Lince) {
            $.Lince = {};
        }

        $.Lince.VideoPlayer = function (el, getData, options) {
            // To avoid scope issues, use 'base' instead of 'this'
            // to reference this class from internal events and functions.
            var base = this;
            base.id = Math.round(Math.random() * 1000000000000);
            // Access to jQuery and DOM versions of element
            base.$el = $(el);
            base.el = el;
            // Add a reverse reference to the DOM object
            base.$el.data("lince.videoPlayer", base);
            base.init = function () {
                base.getData = getData;
                base.options = $.extend(true, $.Lince.VideoPlayer.defaultOptions, options);

                // Put your initialization code here
                base.$el.attr("tabindex", "0");
                (base.$el.data("current")) ? base.$el.focus() : null;
                base.render(base.options);
                base.renderScenes();
                try {
                    base.player = $("#" + base.id + '_videoElement')[0];
                    base.progressBar = $("#" + base.id + '_progressBar')[0];
                    base.playBtn = $("#" + base.id + '_playPause')[0];
                    base.stopBtn = $("#" + base.id + '_stop')[0];
                    base.increaseSpeedBtn = $("#" + base.id + '_increaseSpeed')[0];
                    base.decreaseSpeedBtn = $("#" + base.id + '_decreaseSpeed')[0];
                    base.muteBtn = $("#" + base.id + '_mute')[0];
                    base.volumeBar = $("#" + base.id + '_volumeBar')[0];
                    base.trackListBtn = $("#" + base.id + '_trackList')[0];
                    base.saveSceneBtn = $("#" + base.id + '_saveScene')[0];
                    base.fullScreenBtn = $("#" + base.id + '_fullScreen')[0];
                    base.isNormalScreen = true;
                    base.addEvents();

                } catch (e) {
                    console.log("Something goes wrong in the init");
                }
            };
            /**
             * This function is adding all the events of the plugin
             */
            base.addEvents = function () {
                /**
                 * When clicks on the screen the video is played or paused
                 */
                $(base.player).click(function (event) {
                    event.preventDefault();
                    (base.player.paused) ? base.player.play() : base.player.pause();
                    base.setPlayPauseClass(base.options.templates);
                });

                /**
                 * The play button is calling the previous function
                 */
                $(base.playBtn).click(function (event) {
                    event.preventDefault();
                    $(base.player).click();
                });

                /**
                 * Stops the video
                 */
                $(base.stopBtn).click(function (event) {
                    event.preventDefault();
                    base.player.load();
                    base.setDefault();
                });

                /**
                 * Increase the playback rate of the player
                 */
                $(base.increaseSpeedBtn).click(function (event) {
                    event.preventDefault();
                    (base.player.playbackRate < 2) ? base.setPlaybackRate(base.player.playbackRate + 0.5) : base.setPlaybackRate(2);
                    base.setSpeedInfo(base.player.playbackRate);
                });

                /**
                 * Decrease the playback rate of the player
                 */
                $(base.decreaseSpeedBtn).click(function (event) {
                    event.preventDefault();
                    (base.player.playbackRate > 0.5) ? base.setPlaybackRate(base.player.playbackRate - 0.5) : base.setPlaybackRate(0.5);
                    base.setSpeedInfo(base.player.playbackRate);
                });

                /**
                 * Set the volume to 0
                 */
                $(base.muteBtn).click(function () {
                    base.setVolume(0.0, 0.0);
                });

                /**
                 * shows the track list
                 */
                $(base.trackListBtn).click(function () {
                    base.$el.find(".trackList").slideToggle("fast");
                });

                /**
                 * shows a modal to save an scene
                 */
                $(base.saveSceneBtn).click(function () {
                    base.player.pause();
                    base.setPlayPauseClass(base.options.templates);
                    base.$el.find(".modal").modal("show");
                });

                /**
                 * Set the volume using the volume bar values
                 */
                $(base.volumeBar).mousemove(function () {
                    base.setVolume(0.0, base.volumeBar.value);
                });

                /**
                 * Allows the full screen
                 */
                $(base.fullScreenBtn).click(function (event) {
                    event.preventDefault();
                    if (base.player.requestFullScreen) {
                        base.player.requestFullScreen();
                    } else if (base.player.mozRequestFullScreen) {
                        base.player.mozRequestFullScreen();
                    } else if (base.player.webkitRequestFullScreen) {
                        base.player.webkitRequestFullScreen();
                    } else if (base.player.msRequestFullscreen) {
                        base.player.msRequestFullscreen();
                    }
                    base.isNormalScreen = false;
                    base.player.controls = true;
                });

                /**
                 * Change the progress bar width and sets the timer
                 */
                base.player.ontimeupdate = function () {
                    var percentage = (base.player.currentTime * 100) / base.player.duration;
                    base.setProgressPercentage(percentage + "%");
                    base.setTimeInfo(base.player.currentTime);
                };

                /**
                 * Sets the keycodes for keyboard accesibility, the prevent default is important because the space bar works as a click
                 */
                base.$el.keydown(function (evt) {
                    if (!(base.$el.find(".modal").hasClass('in'))) {
                        switch (evt.keyCode) {
                            case base.options.accessKey.volumeUp:
                                evt.preventDefault();
                                base.setVolume(0.05, base.player.volume);
                                break;
                            case base.options.accessKey.volumeDown:
                                evt.preventDefault();
                                base.setVolume(-0.05, base.player.volume);
                                break;
                            case base.options.accessKey.speedUp:
                                evt.preventDefault();
                                $(base.increaseSpeedBtn).click();
                                break;
                            case base.options.accessKey.speedDown:
                                evt.preventDefault();
                                $(base.decreaseSpeedBtn).click();
                                break;
                            case base.options.accessKey.playPause:
                                evt.preventDefault();
                                $(base.playBtn).click();
                                break;
                            case  base.options.accessKey.fullScreen:
                                evt.preventDefault();
                                $(base.fullScreenBtn).click();
                                break;
                            case base.options.accessKey.stop:
                                evt.preventDefault();
                                $(base.stopBtn).click();
                                break;
                            case base.options.accessKey.mute:
                                evt.preventDefault();
                                $(base.muteBtn).click();
                                break;
                            case base.options.accessKey.trackList:
                                evt.preventDefault();
                                $(base.trackListBtn).click();
                                break;
                            case base.options.accessKey.saveScene:
                                evt.preventDefault();
                                $(base.saveSceneBtn).click();
                                break;
                        }
                    }
                });

                /**
                 * When click on the save button of the modal, the scene is saved
                 */
                base.$el.find(".modal").find("button[data-action='save']").click(function () {
                    var input = base.$el.find(".modal").find('input');
                    base.doStoreTrack({
                        currentTime: base.player.currentTime,
                        videoUrl: base.options.url,
                        title: input.val()
                    });
                    base.renderScenes();
                    input.val("");
                });

                /**
                 * Change the focus to the element
                 */
                base.$el.find(".modal").on('hidden.bs.modal', function () {
                    base.$el.focus();
                });

                /**
                 * Change the focus to the modal
                 */
                base.$el.find(".modal").on('shown.bs.modal', function () {
                    base.$el.find(".modal").find("input").focus();
                });

                /**
                 * Loads the events of full screen
                 */
                base.loadEventExitFullScreen();

                /**
                 * This interval is used to change the dimensions of the desplegable list for the tracks.
                 */
                setInterval(function () {
                    base.$el.find(".trackList")[0].style.marginTop = ($(base.player).height() * -1) + "px";
                    base.$el.find(".trackList").height($(base.player).height());
                }, 300);
            };

            /**
             * This part is formatting the strings to paint the html
             * @param o
             */
            base.render = function (o) {
                var videoTxt = base.format(o.templates.video, {
                    id: base.id + '_videoElement',
                    classCss: o.outerGridConfig +' videoBox',
                    width: o.width,
                    poster: o.poster,
                    sources: base.format(o.templates.source, {src: o.url, type: 'video/mp4'})
                });

                var trackList = base.format(o.templates.ul, {
                    classCss: o.outerGridConfig + " trackList",
                    elements: ""
                });

                var listElements = base.renderInfo(o, "progressBar", "col-xs-2 col-sm-2 col-md-2 col-lg-1 infoSpeed", "li", "x1");
                var auxControl = base.renderControl(o, "progressBar", "spanProgress");
                listElements += base.format(o.templates.li, {
                    classCss: 'col-xs-8 col-sm-8 col-md-8 col-lg-10 progressBarStyle',
                    controls: auxControl
                });
                listElements += base.renderInfo(o, "progressBar", "col-xs-2 col-sm-2 col-md-2 col-lg-1 infoTime", "li", "00:00");

                listElements += base.getLiElement(o, "stop", "button", "btn-group-sm");
                listElements += base.getLiElement(o, "decreaseSpeed", "button", "btn-group-sm");
                listElements += base.getLiElement(o, "playPause", "button", "btn-group");
                listElements += base.getLiElement(o, "increaseSpeed", "button", "btn-group-sm");

                auxControl = base.renderControl(o, "mute", "button");
                auxControl += base.renderInfo(o, "volumeBar", base.getControlClasses("showVolume"), "buttonDrop", "");
                var internalVolumeBar = base.renderControl(o, "volumeBar", "volumeRange");
                auxControl += base.format(o.templates.div, {
                    classCss: "dropdown-menu dropUpList",
                    elements: internalVolumeBar
                });
                listElements += base.format(o.templates.li, {classCss: "btn-group-sm dropup", controls: auxControl});

                listElements += base.getLiElement(o, "trackList", "button", "btn-group-sm");
                listElements += base.getLiElement(o, "saveScene", "button", "btn-group-sm");
                listElements += base.getLiElement(o, "fullScreen", "button", "btn-group-sm");

                var controlsTxt = base.format(o.templates.ul, {
                    classCss: o.outerGridConfig +' controlsBox text-center',
                    elements: listElements
                });

                base.$el.html(videoTxt + trackList + controlsTxt + base.renderModal());
            };

            /**
             * Returns a string representation of a li for control list.
             * @param o
             * @param index
             * @param type
             * @param classForLi
             * @returns {*}
             */
            base.getLiElement = function (o, index, type, classForLi) {
                var auxControl = base.renderControl(o, index, type);
                return base.format(o.templates.li, {classCss: classForLi, controls: auxControl});
            };

            /**
             * Used to create a control, returns the formatted string
             * @param o
             * @param index
             * @param type
             * @returns {string}
             */
            base.renderControl = function (o, index, type) {
                return (o.allowControls[index]) ? base.format(o.templates[type], {
                    id: base.id + "_" + index,
                    classCss: base.getControlClasses(index),
                    title: o.labels[index]
                }) : "";

            };

            /**
             * Used for information tags that depends on the progress bar . It returns the formated string for the information elements.
             * @param o
             * @param dependsOnIndex
             * @param classCss
             * @param type
             * @param controlInside
             * @returns {string}
             */
            base.renderInfo = function (o, dependsOnIndex, classCss, type, controlInside) {
                return (o.allowControls[dependsOnIndex]) ? base.format(o.templates[type], {
                    classCss: classCss,
                    controls: controlInside
                }) : "";
            };

            /**
             * General getter for the control classes, depending on the index it returns a class or another
             * @param index
             * @returns {*}
             */
            base.getControlClasses = function (index) {
                var classes = {
                    progressBar: "progress-bar progressChange",
                    stop: base.format(base.options.templates.classBtn, {icon: "stop"}),
                    decreaseSpeed: base.format(base.options.templates.classBtn, {icon: "backward"}),
                    playPause: base.format(base.options.templates.classBtn, {icon: "play"}),
                    increaseSpeed: base.format(base.options.templates.classBtn, {icon: "forward"}),
                    mute: base.format(base.options.templates.classBtn, {icon: "volume-off"}),
                    showVolume: base.format(base.options.templates.classBtn, {icon: "option-vertical"}) + " btn-slim",
                    volumeBar: "volumeBar",
                    fullScreen: base.format(base.options.templates.classBtn, {icon: "fullscreen"}),
                    trackList: base.format(base.options.templates.classBtn, {icon: "list"}),
                    saveScene: base.format(base.options.templates.classBtn, {icon: "star"}) + " btn-slim"
                };
                return classes[index];
            };

            /**
             * This function is replacing the occurrences between {} of a given string for the arguments (that are represented by a json object)
             * @param str
             * @param args
             * @returns {*}
             */
            base.format = function (str, args) {
                for (var arg in args) {
                    if (args.hasOwnProperty(arg))
                        str = str.replace(new RegExp("\\{" + arg + "\\}", "gi"), args[arg]);
                }
                return str;
            };

            // Video Functions

            /**
             * Used to change the volume from any control defined for that purpose
             * @param valorDiff
             * @param valueOfControl
             */
            base.setVolume = function (valorDiff, valueOfControl) {
                try {
                    valueOfControl = parseFloat(valueOfControl);
                    base.player.volume = parseFloat((valorDiff + valueOfControl).toPrecision(2));
                }
                catch (e) {
                    ((Math.round(base.player.volume) === 1)) ? base.player.volume = 1 : base.player.volume = 0;
                }
                console.log(base.player.volume);
                base.setPositionRange(base.player.volume);
            };

            /**
             * Function needed to hide controls when out of full screen
             */
            base.loadEventExitFullScreen = function () {
                var fullScreenEvents = ["fullscreenchange", "mozfullscreenchange", "webkitfullscreenchange", "MSFullscreenChange"];
                for (var i = 0; i < fullScreenEvents.length; i++) {
                    document.addEventListener(fullScreenEvents[i], base.doExitFullScreen);
                }
            };

            /**
             * When exit the full screen, hide the native buttons
             */
            base.doExitFullScreen = function () {
                if (base.isNormalScreen !== true) {
                    base.player.controls = true;
                    base.isNormalScreen = true;
                } else {
                    base.player.controls = false;
                    base.setPositionRange(base.player.volume);
                    base.setPlayPauseClass(base.options.templates);
                }
            };

            /**
             * Permutes the classes of the play button
             * @param o
             */
            base.setPlayPauseClass = function (o) {
                (base.player.paused) ? base.playBtn.className = base.format(o.classBtn, {icon: "play"}) : base.playBtn.className = base.format(o.classBtn, {icon: "pause"});
            };

            /**
             * Changes the position of the input range for the volume
             * @param value
             */
            base.setPositionRange = function (value) {
                base.volumeBar.value = value;
            };

            /**
             * Changes the width of the progress bar
             * @param percentage
             */
            base.setProgressPercentage = function (percentage) {
                base.progressBar.style.width = percentage;
            };

            /**
             *Changes the playback rate to the given number
             * @param num
             */
            base.setPlaybackRate = function (num) {
                base.player.playbackRate = num;
            };

            /**
             * This method is doing a conversion to show the current time in a MM:SS format
             * @param time
             */
            base.setTimeInfo = function (time) {
                var minutes = Math.floor(time / 60);
                var seconds = Math.floor(time % 60);
                base.$el.find(".infoTime")[0].innerHTML = ((minutes < 10) ? "0" + minutes : minutes) + ":" + ((seconds < 10) ? "0" + seconds : seconds);
            };

            /**
             * This method changes the speed value to inform to the user
             * @param value
             */
            base.setSpeedInfo = function (value) {
                base.$el.find(".infoSpeed")[0].innerHTML = "x" + value;
            };

            /**
             * Resets the default values
             */
            base.setDefault = function () {
                base.setVolume(0.0, 0.5);
                base.setPlaybackRate(1);
                base.setProgressPercentage("0");
                base.setPlayPauseClass(base.options.templates);
                base.setTimeInfo(0);
                base.setSpeedInfo(base.player.playbackRate);
            };

            //Local storage functions
            /**
             * Function used to store information in local storage, Checks if the current track exists and doesn't save it
             * @param track
             */
            base.doStoreTrack = function (track) {
                var tracksStored = localStorage.getItem('tracks');
                var tracks = (tracksStored) ? JSON.parse(tracksStored) : [];
                var ocurrences = tracks.filter(function (o) {
                    return (o.currentTime == track.currentTime && o.videoUrl == track.videoUrl);
                }).length;
                if (ocurrences == 0) {
                    tracks.push(track);
                    localStorage.setItem('tracks', JSON.stringify(tracks));
                }

            };

            /**
             * Function used to paint the tracks on the track list. First gets only the tracks for the current video and then it's sorting the collection by time.
             * Then empties the elements of the track list and paint them (with the each) setting in each element an event to jump in the track.
             */
            base.renderScenes = function () {
                try {
                    var tracksStored = localStorage.getItem('tracks');
                    var tracks = [];
                    var list = base.$el.find(".trackList")[0];

                    if (tracksStored != null) {
                        tracks = JSON.parse(tracksStored);
                        tracks = tracks.sort(function (o1, o2) {
                            return parseFloat(o1.currentTime) - parseFloat(o2.currentTime);
                        });
                        tracks = tracks.filter(function (track) {
                            return (track.videoUrl == base.options.url);
                        });
                        $(list).empty();
                        $.each(tracks, function (index, o) {
                            $(list).append("<li>Track " + index + ": " + o.title + " (" + Math.round(o.currentTime * 1000) / 1000 + "s)</li>", "");
                            $(list).children().last().click(function () {
                                base.setCurrentTime(o.currentTime);
                            });
                        });
                    }
                } catch (e) {
                    console.log("problem rendering scenes");
                }

            };

            /**
             * Sets the time of the player
             * @param n
             */
            base.setCurrentTime = function (n) {
                base.player.currentTime = n
            };

            /**
             * This returns the representation  of a modal in form of string
             * @returns {string}
             */
            base.renderModal = function () {
                var modalString = '<div class="modal fade" role="dialog">';
                modalString += '<div class="modal-dialog modal-sm">';
                modalString += '<div class="modal-content">';
                modalString += '<div class="modal-header">';
                modalString += '<h4 class="modal-title">Save Scene</h4>';
                modalString += '</div>';
                modalString += '<div class="modal-body">';
                modalString += '<input class = "form-control" type = "text" placeholder = "Title of the scene" autofocus/>';
                modalString += '</div>';
                modalString += '<div class="modal-footer">';
                modalString += '<button type="button" class="btn btn-success" data-dismiss="modal" data-action = "save">Save</button>';
                modalString += '<button type="button" class="btn btn-default" data-dismiss="modal" data-action = "cancel">Cancel</button>';
                modalString += '</div>';
                modalString += '</div>';
                modalString += '</div>';
                modalString += '</div>';
                modalString += '</div>';
                return modalString;
            };
            // Run initializer
            base.init();
        };
        $.Lince.VideoPlayer.defaultOptions = {
            url: "media/mov.mp4",
            width: "800",
            poster: "./img/poster.png",
            outerGridConfig:"col-xs-12 col-sm-offset-1 col-sm-10 col-md-offset-3 col-md-6", //this option is in case the user of the plugin wants to change the grid configuration
            labels: {
                progressBar: "Progress bar",
                stop: "Stop",
                decreaseSpeed: "Decrease speed",
                playPause: "Play or Pause",
                increaseSpeed: "Increase speed",
                mute: "Mute",
                volumeBar: "Volume bar",
                trackList: "Track list",
                saveScene: "Save scene",
                fullScreen: "full screen"
            },
            allowControls: {
                progressBar: true,
                stop: true,
                decreaseSpeed: true,
                playPause: true,
                increaseSpeed: true,
                mute: true,
                volumeBar: true,
                trackList: true,
                saveScene: true,
                fullScreen: true
            },
            accessKey: {
                playPause: 32, //space bar
                stop: 48, //0
                mute: 77, //m
                volumeUp: 38,//arrow up
                volumeDown: 40,//arrow down
                speedUp: 39,//arrow right
                speedDown: 37,//arrow left
                trackList: 84, //t
                saveScene: 83, //s
                fullScreen: 122 //f11
            },
            templates: {
                video: "<video id = '{id}' class = '{classCss}' tabindex = '-1' width = '{width}' poster = '{poster}'> {sources} </video>",
                source: "<source src='{src}' type='{type}'> Your browser doesn't support this video",
                ul: "<ul class = '{classCss}'>{elements}</ul>",
                div: "<div class = '{classCss}'>{elements}</div>",
                li: "<li class = '{classCss}'>{controls}</li>",
                button: "<button id = '{id}' tabindex='-1' class = '{classCss}' title = '{title}'></button>",
                buttonDrop: "<button tabindex='-1' class = '{classCss}' data-toggle='dropdown'>{controls}</button>",
                spanProgress: "<span id = '{id}' class = '{classCss}' title = '{title}'/>",
                volumeRange: "<input orient='vertical' type='range' class = '{classCss}' id='{id}' value='0.50' min='0' max='1' step='0.05' title = '{title}'/>",
                classBtn: "btn btn-square glyphicon glyphicon-{icon}"
            }
        };

        /**
         * This function has been modified to accept differents options for different videos getin an array of objects.
         * @param getData
         * @param optionsArray
         * @returns {*}
         */
        $.fn.lince_VideoPlayer = function (getData, optionsArray) {
            var i = 0, options;
            return this.each(function () {
                try {
                    options = (optionsArray[i] === undefined) ? optionsArray[0] : optionsArray[i];
                }
                catch(e){
                    console.log("Error loading array of options: Setting the default options ");
                    options = {};
                }
                (new $.Lince.VideoPlayer(this, getData, options));
                i++;
            });
        };
        // This function breaks the chain, but returns
        // the myCorp.VideoPlayer if it has been attached to the object.
        $.fn.getLince_VideoPlayer = function () {
            this.data("Lince.VideoPlayer");
        };
    })
(jQuery);