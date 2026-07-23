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
import java.util.UUID;

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
            String defaultPassword = System.getenv().getOrDefault("HORIZONAI_ADMIN_PASSWORD",
                UUID.randomUUID().toString().substring(0, 12));
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode(defaultPassword));
            admin.setEmail("admin@horizonai.com");
            admin.setRole("ADMIN");
            userMapper.insert(admin);
            log.info("默认管理员已创建: admin / {}", defaultPassword);
        }
    }

    private void initSampleData() {
        // 只在没有文章时插入示例数据
        if (articleMapper.selectCount(null) > 0) return;

        log.info("开始插入示例数据...");

        // 示例文章
        String[][] samples = {
            {"GPT-5 发布：多模态能力全面升级", "OpenAI 正式发布 GPT-5，支持原生多模态推理，在代码生成和数学推理方面大幅超越前代", "NEWS"},
            {"Vue 4 正式版发布，性能提升显著", "Vue 4 采用新的响应式引擎，编译时优化使打包体积减小 40%，运行时性能提升 2 倍", "NEWS"},
            {"Rust 在 Linux 内核中的使用持续扩大", "Linus Torvalds 表示 Rust for Linux 项目进展顺利，更多驱动将用 Rust 编写", "NEWS"},
            {"kubernetes/kubernetes — 生产级容器编排平台", "Kubernetes 是 CNCF 毕业项目，GitHub 上 Star 数超过 110k，是云原生生态的核心项目", "GITHUB"},
            {"t3-oss/create-t3-app — 全栈应用脚手架", "T3 Stack 整合 Next.js、tRPC、Prisma 等技术栈，提供类型安全的端到端开发体验", "GITHUB"},
            {"langgenius/dify — LLM 应用开发平台", "Dify 是开源的大语言模型应用开发平台，支持可视化编排 AI 工作流，已获 50k+ Star", "GITHUB"},
            {"2026 年 AI Agent 将重塑企业软件架构", "Gartner 预测到 2028 年 60% 的企业应用将内置 AI Agent 能力，从助手模式转向自主决策", "TREND"},
            {"WebAssembly 走出浏览器，成为服务端新势力", "Wasm 在边缘计算和插件系统中的采用快速增长，Docker 宣布原生支持 Wasm 运行时", "TREND"},
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
