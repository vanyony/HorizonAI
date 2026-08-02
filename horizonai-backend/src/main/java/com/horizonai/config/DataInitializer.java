package com.horizonai.config;

import com.horizonai.entity.Article;
import com.horizonai.entity.ArticleTag;
import com.horizonai.entity.Tag;
import com.horizonai.entity.User;
import com.horizonai.mapper.ArticleMapper;
import com.horizonai.mapper.ArticleTagMapper;
import com.horizonai.mapper.TagMapper;
import com.horizonai.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 启动时初始化管理员账号和示例数据
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserMapper userMapper;
    private final ArticleMapper articleMapper;
    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserMapper userMapper, ArticleMapper articleMapper,
                           TagMapper tagMapper, ArticleTagMapper articleTagMapper,
                           PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.articleMapper = articleMapper;
        this.tagMapper = tagMapper;
        this.articleTagMapper = articleTagMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        initAdmin();
        initSampleData();
    }

    private void initAdmin() {
        if (userMapper.selectCount(null) == 0) {
            String adminPassword = System.getenv("HORIZONAI_ADMIN_PASSWORD");
            if (adminPassword == null || adminPassword.isBlank()) {
                throw new IllegalStateException(
                    "首次启动必须设置 HORIZONAI_ADMIN_PASSWORD，拒绝生成或记录默认管理员密码");
            }
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setEmail("admin@horizonai.com");
            admin.setRole("ADMIN");
            userMapper.insert(admin);
            log.info("默认管理员账号已创建: admin");
        }
    }

    private void initSampleData() {
        // 只在没有文章时插入示例数据
        if (articleMapper.selectCount(null) > 0) return;

        log.info("开始插入示例数据...");

        // 示例文章
        String[][] samples = {
            {"【示例】大语言模型能力演进讨论", "用于本地演示的模拟资讯：展示模型能力、评估方法与工程集成的讨论。", "NEWS"},
            {"【示例】前端框架性能优化观察", "用于本地演示的模拟资讯：展示前端构建、响应式渲染与运行时优化主题。", "NEWS"},
            {"【示例】系统编程语言生态动态", "用于本地演示的模拟资讯：展示系统软件中内存安全与并发编程的技术话题。", "NEWS"},
            {"【示例】容器编排项目", "用于本地演示的模拟项目条目：展示云原生基础设施内容的采集与推荐。", "GITHUB"},
            {"【示例】全栈应用脚手架", "用于本地演示的模拟项目条目：展示全栈工程工具链的内容分析。", "GITHUB"},
            {"【示例】大模型应用平台", "用于本地演示的模拟项目条目：展示 AI 应用开发与工作流编排主题。", "GITHUB"},
            {"【示例】智能代理对软件架构的影响", "用于本地演示的模拟趋势：展示技术趋势聚合与研究助手引用能力。", "TREND"},
            {"【示例】WebAssembly 服务端应用", "用于本地演示的模拟趋势：展示边缘计算与扩展机制相关内容。", "TREND"},
        };

        for (String[] s : samples) {
            Article article = new Article();
            article.setTitle(s[0]);
            article.setSummary(s[1]);
            article.setContent(s[1]);
            article.setSourceType(s[2]);
            article.setPublishDate(LocalDate.now());
            article.setImportanceRating(0);
            articleMapper.insert(article);
        }

        // 为文章关联标签：前3条→AI, 4-5→前端, 6→后端, 7-8→云原生
        Tag aiTag = tagMapper.selectList(null).stream()
                .filter(t -> "人工智能".equals(t.getName())).findFirst().orElse(null);
        Tag feTag = tagMapper.selectList(null).stream()
                .filter(t -> "前端开发".equals(t.getName())).findFirst().orElse(null);
        Tag beTag = tagMapper.selectList(null).stream()
                .filter(t -> "后端开发".equals(t.getName())).findFirst().orElse(null);
        Tag cnTag = tagMapper.selectList(null).stream()
                .filter(t -> "云原生".equals(t.getName())).findFirst().orElse(null);
        Tag osTag = tagMapper.selectList(null).stream()
                .filter(t -> "开源项目".equals(t.getName())).findFirst().orElse(null);

        if (aiTag != null) {
            linkTag(1L, aiTag.getId());
            linkTag(2L, aiTag.getId());
        }
        if (feTag != null) {
            linkTag(2L, feTag.getId());
            linkTag(5L, feTag.getId());
        }
        if (beTag != null) {
            linkTag(3L, beTag.getId());
            linkTag(4L, beTag.getId());
        }
        if (cnTag != null) {
            linkTag(7L, cnTag.getId());
            linkTag(8L, cnTag.getId());
        }
        if (osTag != null) {
            linkTag(4L, osTag.getId());
            linkTag(5L, osTag.getId());
            linkTag(6L, osTag.getId());
        }

        log.info("示例数据插入完成: {} 篇文章, {} 个标签关联",
                samples.length, 10);
    }

    private void linkTag(Long articleId, Long tagId) {
        ArticleTag at = new ArticleTag();
        at.setArticleId(articleId);
        at.setTagId(tagId);
        articleTagMapper.insert(at);
    }
}
